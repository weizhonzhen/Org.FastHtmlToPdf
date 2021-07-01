# Org.FastHtmlToPdf


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
				long endTime = System.currentTimeMillis();
				var path = "D:\\tset\\test" + i + ".pdf";
				var file = new File(path);
				try {
					var stream = new FileOutputStream(file);
					stream.write(result);
					stream.flush();
					stream.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
