/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood;

import edu.columbia.stat.wood.deplump.DeplumpStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

/**
 *
 * @author fwood
 */
public class DeplumpServlet extends HttpServlet {

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

                    while (iter.hasNext()) {
                        FileItemStream item = iter.next();
                        String name = item.getFieldName();

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
                                out.println("<html>");
                                out.println("<head>");
                                out.println("<title>DeplumpServlet Error</title>");
                                out.println("</head>");
                                out.println("<body>");
                                out.println("No filename specified for deplumping, press the back button on the browser and choose a file.");
                                out.println("</body>");
                                out.println("</html>");

                                out.close();
                                return;
                            }

                            response.setContentType("application/deplump");
                            response.setHeader("Content-Disposition", "attachment; filename="+filename+".dpl");
                            CountingOutputStream cos = new CountingOutputStream(response.getOutputStream());
                            DeplumpStream ds = new DeplumpStream(cos);
                            
                            byte[] buffer = new byte[1000];

                            Date date = new Date();
                            long now = date.getTime();
                            File tmpfilename = new File("/tmp/deplump/"+now);
                            while(tmpfilename.exists()) {
                                tmpfilename =  new File(tmpfilename.getName()+".1");
                            }
                            FileOutputStream tmpfile = new FileOutputStream(tmpfilename);
                            BufferedOutputStream bfos = new BufferedOutputStream(tmpfile);

                            int stream_length = 0;
                            int length_read =0;
                            while(( length_read = stream.read(buffer))!=-1) {
                                stream_length += length_read;
                                //for (int i=0;i<length_read;i++)
                                //    ds.write(buffer[i]+128);
                                bfos.write(buffer,0,length_read);
                                ds.write(buffer,0,length_read);
                                
                            }
                            ds.flush();
                            ds.close();
                            bfos.flush();
                            bfos.close();

                         /*   Runtime.getRuntime().exec("/bin/cp "+tmpfilename.getName()+" "+tmpfilename.getName()+".pregz");
                            Runtime.getRuntime().exec("/bin/gzip "+tmpfilename.getName()+".pregz");
                            Runtime.getRuntime().exec("/bin/bzip2 "+tmpfilename.getName());

                            File gzippedfile = new File(tmpfilename.getName()+".pregz.gz");
                            File bzip2edfile = new File(tmpfilename.getName()+".bz2");

                            long gzippedfilelength = gzippedfile.length();
                            long bzip2edfilelength = bzip2edfile.length();*/

                            long deplumpedfilelength = cos.numBytesWritten;
                            

                            return;
                            //System.out.println("File field " + name + " with file name "
                            //        + item.getName() + " detected.");
                            // Process the input stream
                            //...
                        }
                    }
                } catch (FileUploadException fue) {
                    fue.printStackTrace();
                }

                /* TODO output your page here */
            }

        } catch (Exception e) {
            if(out == null && bos == null)
                out = response.getWriter();
            else
                out = new PrintWriter(bos);
            response.setContentType("text/html;charset=UTF-8");
                out.println("<html>");
                out.println("<head>");
                out.println("<title>DeplumpServlet Error</title> "+e.getMessage());
                out.println("</head>");
                out.println("<body>");
                out.println("DeplumpServlet exception.");
                out.println("</body>");
                out.println("</html>");

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
