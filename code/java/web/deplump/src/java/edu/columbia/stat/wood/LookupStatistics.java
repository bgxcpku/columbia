/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

/**
 *
 * @author fwood
 */
public class LookupStatistics extends HttpServlet {

    @Resource(name = "deplumpWebsiteDatasource")
    private DataSource deplumpWebsiteDatasource;
    public int NUMLASTRESULTSTODISPLAY = 10;
    public int MIMETYPEDISPLAYLENGTH = 24;

    
    protected String fileSizeString(int length, NumberFormat dec_nf, NumberFormat double_nf) {
        double dlen = length;
        String ret = "";
        if(length < 1048576) {
            ret = dec_nf.format(length);
        }
        else if(length >= 1048576 && length < 1073741824) {
            dlen = (double)length/(double)1048576;
            ret = double_nf.format(dlen);
            ret = ret + " MB";
        } else
        if(length >= 1073741824) {
            dlen = (double)length/(double)1073741824;
            ret = double_nf.format(dlen);
            ret = ret + " MB";
        }
        return ret;
    }

 
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
        PrintWriter out = response.getWriter();
        Connection connection = null;
        try {
            
            DeplumpServletHelper.printHeader(out);
            out.println("<h2>Deplump performance (website uploads)</h2><br><hr></hr>");

            connection = deplumpWebsiteDatasource.getConnection();


            String query =
                    "select sum(length),sum(deplumped_length),sum(gzipped_length),sum(bzip2ed_length) from deplump_website.usage";

            // average compression results
            PreparedStatement ps = connection.prepareStatement(query);


            ResultSet rs = ps.executeQuery(query);
                        if (rs.next()) {
            out.println("<h3>Total</h3><br>");

            out.println("<table>");
            out.println("<tr>");
            out.println("<th></th><th>raw</th><th>deplump</th><th>gzip</th><th>bzip2</th>");
            out.println("</tr>");
            NumberFormat dec_nf = NumberFormat.getInstance();
            dec_nf = dec_nf.getIntegerInstance();
            NumberFormat double_nf = NumberFormat.getInstance();
            double_nf.setMaximumFractionDigits(3);
            double_nf.setMinimumFractionDigits(3);

            NumberFormat pct_nf = NumberFormat.getPercentInstance();
            pct_nf.setMaximumFractionDigits(1);
            pct_nf.setMinimumFractionDigits(1);


                out.println("<tr>");
                int totalLength = rs.getInt(1);
                int deplumpedLength = rs.getInt(2);
                int gzipped_length = rs.getInt(3);
                int bzip2ed_length = rs.getInt(4);

                out.println("<th>Total </th><td>" + fileSizeString(totalLength, dec_nf, double_nf) + "</td><td>" + fileSizeString(deplumpedLength, dec_nf, double_nf) + "</td><td>" +  fileSizeString(gzipped_length, dec_nf, double_nf) + "</td><td>" + fileSizeString(bzip2ed_length, dec_nf, double_nf) + "</td>");
                out.println("</tr>");
                out.println("<th>Reduction </th><td>100&#37;</td><td>" + pct_nf.format((double)deplumpedLength/(double)totalLength) + "</td><td>" +  pct_nf.format((double)gzipped_length/(double)totalLength) + "</td><td>" + pct_nf.format((double)bzip2ed_length/(double)totalLength) + "</td>");
                out.println("</tr>");


            out.println("</table>");
            }


            query =
                    "select mimetype,length,deplumped_length,gzipped_length,bzip2ed_length from deplump_website.usage where id >= (select max(id) from deplump_website.usage)- " + NUMLASTRESULTSTODISPLAY + " ORDER BY id DESC";


            // average compression results
            ps = connection.prepareStatement(query);



            out.println("<hr></hr><h3>Last " + NUMLASTRESULTSTODISPLAY + " uploads: </h3><br>");

            rs = ps.executeQuery(query);
            out.println("<table>");
            out.println("<tr>");
            out.println("<th>mime type</th><th>bytes</th><th>deplump</th><th>gzip</th><th>bzip2</th>");
            out.println("</tr>");
            NumberFormat dec_nf = NumberFormat.getInstance();
            dec_nf = dec_nf.getIntegerInstance();
            NumberFormat double_nf = NumberFormat.getInstance();
            double_nf.setMaximumFractionDigits(3);
            double_nf.setMinimumFractionDigits(3);

            while (rs.next()) {
                out.println("<tr>");
                String mimetype = rs.getString(1);
                if(mimetype == null) {
                    mimetype = "<i>unknown</i>";
                } else {
                if(mimetype.length()>MIMETYPEDISPLAYLENGTH)
                    mimetype  = mimetype.substring(0,MIMETYPEDISPLAYLENGTH-3) + "...";
                }
                int totalLength = rs.getInt(2);
                int deplumpedLength = rs.getInt(3);
                int gzipped_length = rs.getInt(4);
                int bzip2ed_length = rs.getInt(5);



                out.println("<td>" + mimetype + "</td><td>" + fileSizeString(totalLength, dec_nf, double_nf) + "</td><td>" + fileSizeString(deplumpedLength, dec_nf, double_nf) + "</td><td>" +  fileSizeString(gzipped_length, dec_nf, double_nf) + "</td><td>" + fileSizeString(bzip2ed_length, dec_nf, double_nf) + "</td>");
                out.println("</tr>");

            }

            out.println("</table>");


             query =
                    "select mimetype,sum(length),sum(deplumped_length),sum(gzipped_length),sum(bzip2ed_length) from deplump_website.usage group by mimetype";


            // average compression results
            ps = connection.prepareStatement(query);
            rs = ps.executeQuery(query);

            out.println("<hr></hr><br><h3>Average bits in compressed stream per byte of original uncompressed stream grouped by mime type: </h3><br>");
            out.println("<table>");
            out.println("<tr>");
            out.println("<th>mimetype</th><th>deplump</th><th>gzip</th><th>bzip2</th>");
            out.println("</tr>");
            
            while (rs.next()) {
                out.println("<tr>");
                String mimetype = rs.getString(1);
                if(mimetype == null) {
                    mimetype = "<i>unknown</i>";
                } else {
                if(mimetype.length()>MIMETYPEDISPLAYLENGTH)
                    mimetype  = mimetype.substring(0,MIMETYPEDISPLAYLENGTH-3) + "...";
                }
                int totalLength = rs.getInt(2);
                int deplumpedLength = rs.getInt(3);
                int gzipped_length = rs.getInt(4);
                int bzip2ed_length = rs.getInt(5);

                out.println("<td>" + mimetype + "</td><td>" + double_nf.format((double) deplumpedLength * 8 / (double) totalLength) + "</td><td>" + double_nf.format((double) gzipped_length * 8 / (double) totalLength) + "</td><td>" + double_nf.format((double) bzip2ed_length * 8 / (double) totalLength) + "</td>");
                out.println("</tr>");

            }

            out.println("</table>");


            rs.beforeFirst();

            out.println("<hr></hr><br><h3>Website usage: total number of bytes compressed grouped by mime type:</h3><br>");

            out.println("<table>");
            out.println("<tr>");
            out.println("<th>mime type</th><th>bytes</th><th>deplump</th><th>gzip</th><th>bzip2</th>");
            out.println("</tr>");
            while (rs.next()) {
                out.println("<tr>");
                String mimetype = rs.getString(1);
                if(mimetype == null) {
                    mimetype = "<i>unknown</i>";
                } else {
                if(mimetype.length()>MIMETYPEDISPLAYLENGTH)
                    mimetype  = mimetype.substring(0,MIMETYPEDISPLAYLENGTH-3) + "...";
                }
                int totalLength = rs.getInt(2);
                int deplumpedLength = rs.getInt(3);
                int gzipped_length = rs.getInt(4);
                int bzip2ed_length = rs.getInt(5);

                out.println("<td>" + mimetype + "</td><td>" + fileSizeString(totalLength, dec_nf, double_nf) + "</td><td>" + fileSizeString(deplumpedLength, dec_nf, double_nf)  + "</td><td>" + fileSizeString(gzipped_length, dec_nf, double_nf) + "</td><td>" + fileSizeString(bzip2ed_length, dec_nf, double_nf) + "</td>");
                out.println("</tr>");

            }

            out.println("</table>");


            DeplumpServletHelper.printFooter(out);
            connection.close();


        } catch (Exception e) {
             if(out == null)
                out = response.getWriter();
             try {
             if(connection!=null && !connection.isClosed())
                 connection.close();
            } catch (Exception ee) {
                DeplumpServletHelper.printHeader(out);
                out.println("Please notify <a href=\"mailto:problem@deplump.com\">problem@deplump.com</a>");
                out.println("Plesae include the following debugging information in the email<br><br>");
                ee.printStackTrace(out);
                DeplumpServletHelper.printFooter(out);
            }

            response.setContentType("text/html;charset=UTF-8");
                DeplumpServletHelper.printHeader(out);
                out.println("Please notify <a href=\"mailto:problem@deplump.com\">problem@deplump.com</a>");
                out.println("Plesae include the following debugging information in the email<br><br>");
                e.printStackTrace(out);
                DeplumpServletHelper.printFooter(out);

            out.close();

            e.printStackTrace();
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
