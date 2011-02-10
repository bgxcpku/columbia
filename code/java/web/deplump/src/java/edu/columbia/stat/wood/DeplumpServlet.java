/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood;

import edu.columbia.stat.wood.deplump.DeplumpStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.zip.GZIPOutputStream;
import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.tools.bzip2.CBZip2OutputStream;

/**
 *
 * @author fwood
 */
public class DeplumpServlet extends HttpServlet {

    @Resource(name = "deplumpWebsiteDatasource")
    private DataSource deplumpWebsiteDatasource;
    public final static int MAXSTREAMLENGTH = 2*1048576;

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = null;
        BufferedOutputStream bos = null;
        Connection c = null;
        try {
            // Check that we have a file upload request
            boolean isMultipart = ServletFileUpload.isMultipartContent(request);

            if (!isMultipart) {
                out = response.getWriter();
                response.setContentType("text/html;charset=UTF-8");
                out.println("<html>");
                out.println("<head>");
                out.println("<title>DeplumpServlet Error</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("DeplumpServlet called with non multipart form data.");
                out.println("</body>");
                out.println("</html>");
            } else {

                // Create a new file upload handler
                ServletFileUpload upload = new ServletFileUpload();

                try {
                    // Parse the request
                    FileItemIterator iter = upload.getItemIterator(request);

                    if (iter.hasNext()) {
                        FileItemStream item = iter.next();
                        String name = item.getFieldName();
                        String contentType = item.getContentType();
                        BufferedInputStream stream = new BufferedInputStream(item.openStream());
                        if (item.isFormField()) {
                            System.out.println("Form field " + name + " with value "
                                    + Streams.asString(stream) + " detected.");
                        } else {
                            String filename = item.getName();
                            if (filename == null || filename.equals("")) {
                                if (out == null && bos == null) {
                                    out = response.getWriter();
                                } else {
                                    out = new PrintWriter(bos);
                                }
                                response.setContentType("text/html;charset=UTF-8");
                                DeplumpServletHelper.printHeader(out);
                                out.println("No filename specified for deplumping, press the back button on the browser and choose a file.");
                                DeplumpServletHelper.printFooter(out);


                                out.close();
                                return;
                            }



                            int responseBufferSize = response.getBufferSize();

                            ByteArrayOutputStream temp_stream = new ByteArrayOutputStream(MAXSTREAMLENGTH+100000);

                            CountingOutputStream ds_cos = new CountingOutputStream(temp_stream);
                            //CountingOutputStream ds_cos = new CountingOutputStream(new FileOutputStream("/dev/null"));

                            CountingOutputStream gzip_cos = new CountingOutputStream(new FileOutputStream("/dev/null"));
                            CountingOutputStream bzip2_cos = new CountingOutputStream(new FileOutputStream("/dev/null"));

                            DeplumpStream ds = new DeplumpStream(ds_cos);
                            GZIPOutputStream gzs = new GZIPOutputStream(gzip_cos);
                            CBZip2OutputStream bzs = new CBZip2OutputStream(bzip2_cos);

                            byte[] buffer = new byte[responseBufferSize];


                            /* Date date = new Date();
                            long now = date.getTime();
                            File tmpfilename = new File("/tmp/deplump/"+now);
                            while(tmpfilename.exists()) {
                            tmpfilename =  new File(tmpfilename.getName()+".1");
                            }
                            FileOutputStream tmpfile = new FileOutputStream(tmpfilename);
                            BufferedOutputStream bfos = new BufferedOutputStream(tmpfile);
                             */
                            int stream_length = 0;
                            int length_read = 0;
                            while ((length_read = stream.read(buffer)) != -1) {
                                if (stream_length + length_read > MAXSTREAMLENGTH) {
                                    length_read = MAXSTREAMLENGTH - stream_length;
                                    stream_length = MAXSTREAMLENGTH;

                                } else {
                                    stream_length += length_read;
                                }
                                
                                try {
                                  

                                ds.write(buffer, 0, length_read);
                                gzs.write(buffer, 0, length_read);
                                bzs.write(buffer, 0, length_read);
                                
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                
                                if (stream_length == MAXSTREAMLENGTH) {
                                    stream.close();
                                    break;
                                }

                            }
                            ds.flush();
                            ds.close();
                            gzs.flush();
                            gzs.close();
                            bzs.flush();
                            bzs.close();

                            byte[] deplumped_stream = temp_stream.toByteArray();
                            response.setContentType("application/deplump");
                            response.setContentLength(deplumped_stream.length);
                            response.setHeader("Content-Disposition", "attachment; filename=" + filename + ".dpl");
                            response.setHeader("Content-Transfer-Encoding","binary");

                            response.flushBuffer();
                            ServletOutputStream sos = response.getOutputStream();
                            sos.write(deplumped_stream, 0, deplumped_stream.length);
                            //bfos.flush();
                            //bfos.close();

                            /*   Runtime.getRuntime().exec("/bin/cp "+tmpfilename.getName()+" "+tmpfilename.getName()+".pregz");
                            Runtime.getRuntime().exec("/bin/gzip "+tmpfilename.getName()+".pregz");
                            Runtime.getRuntime().exec("/bin/bzip2 "+tmpfilename.getName());

                            File gzippedfile = new File(tmpfilename.getName()+".pregz.gz");
                            File bzip2edfile = new File(tmpfilename.getName()+".bz2");

                            long gzippedfilelength = gzippedfile.length();
                            long bzip2edfilelength = bzip2edfile.length();*/

                            int deplumpedstreamlength = ds_cos.numBytesWritten;
                            int gzipedfilelength = gzip_cos.numBytesWritten;
                            int bzip2edfilelength = bzip2_cos.numBytesWritten;


                             c = deplumpWebsiteDatasource.getConnection();



                            PreparedStatement ps = c.prepareStatement("insert into deplump_website.usage (mimetype, deplump_version, trained_predictive_model_uri, length, deplumped_length, gzipped_length, bzip2ed_length, remote_host, remote_addr, remote_user) values (?,?,?,?,?,?,?,?,?,?)");
                            ps.setString(1, contentType); // mimetype
                            ps.setInt(2, 0); // deplump version
                            ps.setInt(3, 0); // deplump training set
                            ps.setInt(4, stream_length); // length
                            ps.setInt(5, deplumpedstreamlength);
                            ps.setInt(6, gzipedfilelength);
                            ps.setInt(7, bzip2edfilelength);
                            String remoteHost = request.getRemoteHost();
                            if (remoteHost == null) {
                                ps.setString(8, "");

                            } else {
                                if (remoteHost.length() > 65535) {
                                    remoteHost = remoteHost.substring(0, 65535);
                                }
                                ps.setString(8, remoteHost);
                            }
                            String remoteAddr = request.getRemoteAddr();
                            if (remoteAddr == null) {
                                ps.setString(9, "");
                            } else {
                                if (remoteAddr.length() > 65535) {
                                    remoteAddr = remoteAddr.substring(0, 65535);
                                }
                                ps.setString(9, remoteAddr);
                            }
                            String remoteUser = request.getRemoteUser();
                            if (remoteUser == null) {
                                ps.setString(10, "");
                            } else {
                                if (remoteUser.length() > 65535) {
                                    remoteUser = remoteUser.substring(0, 65535);
                                }
                                ps.setString(10, remoteUser);
                            }
                            ps.executeUpdate();
                            
                            c.close();
                            return;
                            //System.out.println("File field " + name + " with file name "
                            //        + item.getName() + " detected.");
                            // Process the input stream
                            //...
                        }
                    } else {
                        if (out == null && bos == null) {
                            out = response.getWriter();
                        } else {
                            out = new PrintWriter(bos);
                        }
                        response.setContentType("text/html;charset=UTF-8");
                                        DeplumpServletHelper.printHeader(out);

                        out.println("DeplumpServlet Error, no files submitted for deplumping");
                        
                        out.println("DeplumpServlet exception.");
                                        DeplumpServletHelper.printFooter(out);


                        out.close();

                    }
                } catch (FileUploadException fue) {
                    fue.printStackTrace();
                }

                /* TODO output your page here */
            }

        } catch (Exception e) {
            if (out == null && bos == null) {
                out = response.getWriter();
            } else {
                out = new PrintWriter(bos);
            }

            try {
             if(c!=null && !c.isClosed())
                 c.close();
            } catch (Exception ee) {
                DeplumpServletHelper.printHeader(out);
                out.println("Please notify <a href=\"mailto:problem@deplump.com\">problem@deplump.com</a>");
                out.println("Plesae include the following debugging information in the email<br><br>");
                ee.printStackTrace(out);
                DeplumpServletHelper.printFooter(out);
            }

            response.setContentType("text/html;charset=UTF-8");
            DeplumpServletHelper.printHeader(out);
            out.println("Please help us by <a href=\"mailto:problem@deplump.com\">emailing</a> the following error message to us as well as a description of what you were doing when we screwed up.  Thank you.<br>");
            e.printStackTrace(out);
            DeplumpServletHelper.printFooter(out);


            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
