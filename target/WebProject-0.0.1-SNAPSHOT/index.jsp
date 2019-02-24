<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>九九乘法表</title>
</head>
<body>
666
	<%
	//打印9×9的表格
	out.println("<table border=\"1\">") ;
	for(int i=1;i<10;i++)
	{
		out.println("<tr>") ;
		for(int j=1;j<=i;j++)
		{
			out.println("<td>"+i*j+"</td>") ;
		}
		out.println("</tr>");
	}
	out.println("</table>");
	%>
</body>
</html>