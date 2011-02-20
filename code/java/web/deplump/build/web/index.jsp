<%-- 
    Document   : index
    Created on : Aug 17, 2010, 8:49:33 PM
    Author     : fwood
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Homepage for Deplump</title>
    </head>
    To Deplump a file:
    <body>
        <form action="DeplumpServlet" enctype="multipart/form-data" method="post">

Browse to upload file to deplump:<br>
<input type="file" name="datafile" size="40">


<input type="submit" value="Deplump">
</form>
        <p></p>
        To Plump a file:

         <form action="PlumpServlet" enctype="multipart/form-data" method="post">

Browse to upload a deplumped file to plump:<br>
<input type="file" name="datafile" size="40">


<input type="submit" value="Plump">
</form>

    </body>
</html>
