/*
 * Copyright (c) 2007 Yaroslav Stavnichiy, yarosla@gmail.com
 *
 * Latest version of this software can be obtained from:
 *   http://web-tec.info/WikiParser/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * If you make use of this code, I'd appreciate hearing about it.
 * Comments, suggestions, and bug reports welcome: yarosla@gmail.com
 */

package ys.wikiparser.demo;

import java.io.*;
import java.net.*;

import static ys.wikiparser.Utils.*;
import ys.wikiparser.WikiParser;

/**
 * This example program illustrates usage of WikiParser class.
 *
 * @author Yaroslav Stavnichiy (yarosla@gmail.com)
 *
 */
public class WikiParserDemo {

  /**
   * DemoParser - customized WikiParser.
   * Customization is done by overriding appendXxx() methods.
   * This allows implementation-specific handling of hyperlinks,
   * images, and placeholders.
   *
   */
  private static class DemoParser extends WikiParser {
    public DemoParser(String wikiText) {
      super(wikiText);
    }

    public static String renderXHTML(String wikiText) {
      return new DemoParser(wikiText).toString();
    }

    @Override
    protected void appendImage(String text) {
      super.appendImage(text);
    }

    @Override
    protected void appendLink(String text) {
      String[] link=split(text, '|');
      URI uri=null;
      try { // validate URI
        uri=new URI(link[0].trim());
      }
      catch (URISyntaxException e) {
      }
      if (uri!=null && uri.isAbsolute()) {
        sb.append("<a href=\""+escapeHTML(uri.toString())+"\" rel=\"nofollow\">");
        sb.append(escapeHTML(unescapeHTML(link.length>=2 && !isEmpty(link[1].trim())? link[1]:link[0])));
        sb.append("</a>");
      }
      else {
        sb.append("<a href=\"http://en.wikipedia.org/wiki/"+escapeHTML(escapeURL(link[0]))+"\" title=\"Wikipedia link\">");
        sb.append(escapeHTML(unescapeHTML(link.length>=2 && !isEmpty(link[1].trim())? link[1]:link[0])));
        sb.append("</a>");
      }
    }

    @Override
    protected void appendMacro(String text) {
      if ("TOC".equals(text)) {
        sb.append("{{ My table of contents }}");
      }
      else {
        super.appendMacro(text);
      }
    }
  }

  public static String escapeURL(String s) {
    try {
      return URLEncoder.encode(s, "utf-8");
    }
    catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static String readFromFile(String filePath, String encoding) {
    InputStream is=null;
    try {
      is=new FileInputStream(filePath);
      return readFromStream(is, encoding);
    }
    catch (Exception e) {
      e.printStackTrace();
      return null;
    }
    finally {
      if (is!=null) try { is.close(); } catch (IOException e) {}
    }
  }

  public static String readFromStream(InputStream is, String encoding) {
    StringBuffer sb=new StringBuffer();
    try {
      InputStreamReader isr=new InputStreamReader(is, encoding);
      char[] cbuf=new char[4*1024];
      int len;
      while ((len=isr.read(cbuf))>=0) sb.append(cbuf, 0, len);
      //isr.close();
      return sb.toString();
    }
    catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public static boolean writeToFile(String content, String filePath, String encoding) {
    OutputStream os=null;
    try {
      os=new FileOutputStream(filePath);
      return writeToStream(content, os, encoding);
    }
    catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    finally {
      if (os!=null) try { os.close(); } catch (IOException e) {}
    }
  }

  public static boolean writeToStream(String content, OutputStream os, String encoding) {
    try {
      OutputStreamWriter osw=new OutputStreamWriter(os, encoding);
      osw.write(noNull(content));
      osw.flush();
      return true;
    }
    catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  public static void main(String[] args) {
    if (args.length<2) {
      System.err.println("Usage: java -jar WikiParser.jar input.txt output.htm");
      return;
    }
    String wikiText=readFromFile(args[0], "utf-8");
    String htmlText="<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
      +"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">"
      +"<html xmlns=\"http://www.w3.org/1999/xhtml\">"
      +"<head><title>WikiParser</title>"
      +"<style type=\"text/css\">div.indent{margin-left:20px;} span.underline{text-decoration:underline;}</style>"
      +"</head>"
      +"<body>"
      +DemoParser.renderXHTML(wikiText)
      +"</body></html>";
    writeToFile(htmlText, args[1], "utf-8");
  }
}
