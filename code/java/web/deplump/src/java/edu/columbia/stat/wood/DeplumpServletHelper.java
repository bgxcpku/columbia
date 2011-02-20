/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood;

import java.io.PrintWriter;

/**
 *
 * @author fwood
 */
public class DeplumpServletHelper {
    static  public void printHeader(PrintWriter out) {
        out.println("<html>");
          out.println("<head>");
    out.println("<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />");
    out.println("<meta name=\"description\" content=\"Homepage for the Deplump family of general purpose lossless compressors\" />");
    out.println("<meta name=\"keywords\" content=\"deplump, plump, sequence memoizer, lossless compression, probabilistic lossless compression, range coding, ppm, ppmz, gzip, bzip2\" />");
    out.println("<meta name=\"author\" content=\"Frank Wood, Nicholas Bartlett  \" />");
    out.println("<meta name=\"generator\" content=\"webgen - http://webgen.rubyforge.org\" />");
    out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"default.css\" media=\"screen,projection\" />");
    out.println("<title>deplump general purpose lossless compression</title>");
    out.println("<script type=\"text/javascript\">");

  out.println("var _gaq = _gaq || [];");
  out.println("_gaq.push(['_setAccount', 'UA-18916263-1']);");
  out.println("_gaq.push(['_setDomainName', 'none']);");
  out.println("_gaq.push(['_setAllowLinker', true]);");
  out.println("_gaq.push(['_trackPageview']);");

  out.println("(function() {");
  out.println("  var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;");
  out.println("  ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';");
  out.println("  var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);");
  out.println("})();");

out.println("</script>");
  out.println("</head>");

  out.println("<body>");
    out.println("<div id=\"wrap\">");
      out.println("<div id=\"header\">");
        out.println("<h1><a href=\"index.html\">deplump&#0153;</a></h1>");
        out.println("<p><strong>lossless data compression</strong></p>");
      out.println("</div>");

      out.println("<div id=\"avmenu\">");
        out.println("<h2 class=\"hide\">Site menu:</h2>");
        out.println("<ul><li class=\"webgen-menu-level1\"><a href=\"index.html\">Demo</a></li><li class=\"webgen-menu-level1\"><a href=\"performance.html\">Performance</a></li><li class=\"webgen-menu-level1 webgen-menu-submenu\"><a href=\"About/index.html\">About</a></li><li class=\"webgen-menu-level1\"><a href=\"contact.html\">Contact</a></li></ul>");

      out.println("</div>");

      out.println("<div id=\"content\">");
    }

    public static void printFooter(PrintWriter out) {
        out.println("</div>");

      out.println("<div id=\"footer\">");
        out.println("<p>Copyright &copy; 2010</p>");
      out.println("</div>");
    out.println("</div>");
  out.println("</body>");
            out.println("</html>");
    }
}
