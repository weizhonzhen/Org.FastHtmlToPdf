# Org.FastHtmlToPdf
```csharp
    var doc = new ImageDocument();
    var result = FastHtmlToImage.convert(doc, html);

    var doc = new PdfDocument();
    var header = new Header();
    header.setLine(true);
    header.setSpacing(10);
    header.setCenter("头部22dadasd22asdada2头部");
    doc.setHeader(header);

    Footer footer = new Footer();
    footer.setLine(true);
    footer.setSpacing(10);
    footer.setCenter("头部22dadasd22asdada2头部");
    doc.setFooter(footer);

    var result = FastHtmlToPdf.convert(doc, html);
```
