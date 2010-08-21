/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood;

import edu.columbia.stat.wood.deplump.PlumpStream;
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
public class PlumpServlet extends HttpServlet {

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = null;
        BufferedOutputStream bos = null;
        try {  // Check that we have a file upload request
            boolean isMultipart = ServletFileUpload.isMultipartContent(request);

            if (!isMultipart) {
                out = response.getWriter();
                response.setContentType("text/html;charset=UTF-8");
                out.println("<html>");
                out.println("<head>");
                out.println("<title>PlumpServlet Error</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("PlumpServlet called with non multipart form data.");
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

                        PlumpStream stream = new PlumpStream(new BufferedInputStream(item.openStream()));
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
                                out.println("No filename specified for plumping.  Press the back button on the browser and select a deplumped file.");
                                out.println("</body>");
                                out.println("</html>");

                                out.close();
                                return;
                            }


                            response.setContentType("application/plump");

                            filename = filename.substring(0, filename.lastIndexOf("."));

                            response.setHeader("Content-Disposition", "attachment; filename=" + filename);

                            bos = new BufferedOutputStream(response.getOutputStream());



                            //int b = 0;
                            //while ((b = stream.read()) != -1) {
                            //    bos.write(b - 128);
                            //}


                            byte[] buffer = new byte[1000];
                            int length_read =0;
                            while(( length_read = stream.read(buffer))!=-1) {
                                bos.write(buffer,0,length_read);
                            }

                            bos.flush();
                            bos.close();

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
            out.println("PlumpServlet exception. " + e.getMessage());
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
