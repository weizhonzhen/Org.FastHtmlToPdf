# Org.FastHtmlToPdf
```csharp
try (var pdf = new FastHtmlToImage()) {
    var doc = new ImageDocument();
    var result = pdf.convert(doc, html);
}

try (var pdf = new FastHtmlToPdf()) {
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

    var result = pdf.convert(doc, html);
}
```
