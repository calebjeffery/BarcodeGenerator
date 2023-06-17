
/*
 * This class is part of the book "iText in Action - 2nd Edition"
 * written by Bruno Lowagie (ISBN: 9781935182610)
 * For more info, go to: http://itextpdf.com/examples/
 * This example only works with the AGPL version of iText.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.codec.DecoderException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.itextpdf.barcodes.Barcode128;
import com.itextpdf.kernel.color.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfNumber;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.border.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class BarcodeGenerator {

	/** The resulting PDF. */
	private static final String RESULT = "TestBarcodes.pdf";

	private static Namespace res;
	private static String barcodes = "(01) 0 0827002 19628 5 (17) 221218 (10) A123456,(01) 1 0827002 19628 2 (17) 221218 (30) 5 (10) A123456,(01) 0 0827002 20079 1 (11) 191218 (10) A123456";

	/**
	 * Generates a PDF file with different types of barcodes.
	 *
	 * @param args
	 *            no arguments needed here
	 * @throws DocumentException
	 * @throws IOException
	 * @throws TransformerException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws DecoderException
	 */
	public static void main(String[] args) throws IOException, Exception, ParserConfigurationException, SAXException,
			TransformerException, DecoderException {
		ArgumentParser parser = ArgumentParsers.newFor("BarcodeGenerator").build().defaultHelp(false)
				.description("Builds a Pdf of Code128 Barcodes using a xml list of lots and barcodes.");
		parser.addArgument("-o", "--output").required(true).help("Specify the filename of the pdf output.");
		parser.addArgument("-t", "--xmlType").choices("URL", "FILE").required(true).setDefault("URL")
				.help("Specify how the xml file is to be retrieved.");
		parser.addArgument("-p", "--xmlPath").required(true).metavar("http:/path/to/cgi-bin or c:/path/to/barcodes.xml")
				.help("the url or filepath of the barcode list xml.");
		parser.printHelp();
		Namespace ns = null;
		String[] barcodeArr = barcodes.split(",");
		DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
		org.w3c.dom.Document document = documentBuilder.newDocument();
		// root element
		org.w3c.dom.Element root = document.createElement("BARCODES");
		document.appendChild(root);
		// employee element
		org.w3c.dom.Element code = document.createElement("Code");
		code.setTextContent("0");
		root.appendChild(code);

		for (String bc : barcodeArr) {
			org.w3c.dom.Element data = document.createElement("BARCODE_DATA");
			org.w3c.dom.Element descriptionElem = document.createElement("Description");
			descriptionElem.setTextContent(bc);
			org.w3c.dom.Element barcodeElem = document.createElement("Barcode");
			barcodeElem.setTextContent(bc.replaceAll("[\\(\\)]", "").replaceAll(" ", ""));
			data.appendChild(descriptionElem);
			data.appendChild(barcodeElem);
			root.appendChild(data);
			// data = document.createElement("BARCODE_DATA");
			// descriptionElem = document.createElement("Description");
			// descriptionElem.setTextContent(bc.split("-")[1]);
			// barcodeElem = document.createElement("Barcode");
			// barcodeElem.setTextContent(bc.split("-")[1]);
			// data.appendChild(descriptionElem);
			data.appendChild(barcodeElem);
			root.appendChild(data);
		}
		// create the xml file
		// transform the DOM Object to an XML File
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource domSource = new DOMSource(document);

		try {
			res = parser.parseArgs(args);
			StreamResult streamResult = new StreamResult(new File((String) res.get("xmlPath")));
			transformer.transform(domSource, streamResult);
			new BarcodeGenerator().createPdf(res.get("output"), res.get("xmlType"), res.get("xmlPath"));
			System.out.println("");
			System.out.println("Output:" + res.get("output"));
		} catch (ArgumentParserException e) {
			parser.handleError(e);
			System.exit(1);
		} catch (IOException e) {
			System.out.println("");
			System.err.println("Unable to open file:" + e.getMessage());
			System.exit(1);
		}

		//
	}

	public static float millimetersToPoints(float mm, PdfPage page) {
		PdfNumber userUnit = page.getPdfObject().getAsNumber(PdfName.UserUnit);
		float userUnitValue = userUnit == null ? 2.83465f : userUnit.floatValue();
		return mm * userUnitValue;
	}

	/**
	 * Creates a PDF document.
	 *
	 * @param filename
	 *            the path to the new PDF document
	 * @throws DocumentException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws TransformerException
	 * @throws DecoderException
	 */
	public void createPdf(String filename, String type, String xmlPath) throws IOException, Exception,
			ParserConfigurationException, SAXException, TransformerException, DecoderException {
		// URL url = new URL(
		// "http://aus-jbdev-d3.japan.cau.cookgroup.nao/cgi-bin/getjdata.cgi?APPNAME=GENERATE.AVAIL.TROLLEY.BARCODES&SERVICECENTER=JPSC&USERID=cjeffery&SESSIONID=666B9ED9AE7D403044DE844866E23BAB&AVAILONLY=TRUE");
		// URLConnection conn = url.openConnection();
		InputStream inputStream = null;
		if (type.equalsIgnoreCase("URL")) {
			URL url = new URL(xmlPath);
			URLConnection conn = url.openConnection();
			inputStream = conn.getInputStream();
		} else if (type.equalsIgnoreCase("FILE")) {
			inputStream = new FileInputStream(xmlPath);
		}

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		org.w3c.dom.Document doc = builder.parse(inputStream);
		NodeList nList = doc.getElementsByTagName("BARCODE_DATA");

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer xform = transformerFactory.newTransformer();
		OutputStream xmlOutputStream = null;
		xmlOutputStream = System.out;
		xmlOutputStream = new FileOutputStream(new File("C:/temp/barcodesXml.xml"));

		// that’s the default xform; use a stylesheet to get a real one
		xform.transform(new DOMSource(doc), new StreamResult(xmlOutputStream));

		// step 1

		// step 2
		PdfWriter writer = new PdfWriter(filename);
		PdfDocument pdfDocument = new PdfDocument(writer);
		pdfDocument.setDefaultPageSize(PageSize.A4);
		// step 3
		Document document = new Document(pdfDocument);
		// step 4
		PdfFont smallfont;
		Table table = new Table(new float[] { PageSize.A4.getWidth() / 2 });
		Cell cell = new Cell(1, 10).add(new Paragraph("BARCODES"));
		cell.setTextAlignment(TextAlignment.CENTER);
		cell.setPadding(5);
		cell.setBackgroundColor(new DeviceRgb().GRAY);
		table.addCell(cell);

		// table.addCell("LOT");
		table.addCell("Barcode");
		// table.addCell(barcodeCell);
		// table.addCell(lotCell);
		// table.addCell(barcodeCell);
		HashMap<String, String> lots = new HashMap<>();
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				org.w3c.dom.Element eElement = (org.w3c.dom.Element) nNode;
				String lot = eElement.getElementsByTagName("Description").item(0).getTextContent();
				String barcode = eElement.getElementsByTagName("Barcode").item(0).getTextContent();
				if (lots.get(lot) == null) {
					// CODE 128
					lots.put(lot, barcode);

					Barcode128 code128 = new Barcode128(pdfDocument);
					code128.setCodeType(Barcode128.CODE128);
					code128.setCode(lot);
					Image code128Image = new Image(code128.createFormXObject(pdfDocument));
					Cell barCodeCell = new Cell().add(code128Image);
					barCodeCell.setPaddingTop(100);
					barCodeCell.setPaddingRight(10);
					barCodeCell.setPaddingBottom(25);
					barCodeCell.setPaddingLeft(10);
					barCodeCell.setBorder(Border.NO_BORDER);
					// table.addCell(lot);
					table.addCell(barCodeCell);
				}
			}
		}
		// table.setHorizontalAlignment(PdfPTable.ALIGN_MIDDLE);
		document.add(table);
		document.close();
	}

}