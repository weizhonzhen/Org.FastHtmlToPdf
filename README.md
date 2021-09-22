# Org.FastHtmlToPdf
```csharp
    var doc = new ImageDocument();
    var result = FastHtmlToImage.convert(doc, html);

    var doc = new PdfDocument();
    var header = new Header();
    header.setLine(true);
    header.setSpacing(10);
    //header.setCenter("头部22dadasd22asdada2头部");
    footer.setUrl("http://127.0.0.1:8080/Footer");
    doc.setHeader(header);

    Footer footer = new Footer();
    footer.setLine(true);
    footer.setSpacing(10);
    //footer.setCenter("头部22dadasd22asdada2头部");
    header.setUrl("http://127.0.0.1:8080/Header");
    doc.setFooter(footer);

    var result = FastHtmlToPdf.convert(doc, html);
```
