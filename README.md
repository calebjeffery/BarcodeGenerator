# Barcode Generator

This project generates a PDF file containing different types of barcodes using the Code128 barcode format. 
It reads a list of barcodes and corresponding descriptions from an XML file, and creates a PDF document with the barcodes.

Please note that I based this off an example found in iText in Action - 2nd Edition written by Bruno Lowagie (ISBN: 9781935182610)

## Features

- Supports retrieval of the barcode list XML from either a URL or a local file.
- Generates Code128 barcodes for each entry in the XML.
- Creates a PDF document with the generated barcodes.

## Prerequisites

- Java Development Kit (JDK) 8 or higher
- Apache Maven (for building the project)

## Usage

1. Clone the repository:

   ```bash
   git clone https://github.com/your-username/barcode-generator.git
   ```

2. Navigate to the project directory:

   ```bash
   cd barcode-generator
   ```

3. Build the project using Maven:

   ```bash
   mvn package
   ```

4. Run the application with the desired arguments:

   ```bash
   java -jar target/barcode-generator.jar -o <output-file> -t <xml-type> -p <xml-path>
   ```

   Replace `<output-file>` with the desired filename for the generated PDF, `<xml-type>` with either "URL" or "FILE" depending on how the XML file is retrieved, and `<xml-path>` with the URL or filepath of the barcode list XML.

   Example:

   ```bash
   java -jar target/barcode-generator.jar -o barcodes.pdf -t FILE -p /path/to/barcodes.xml
   ```

5. The generated PDF file will be saved as `<output-file>` in the current directory.

## Dependencies

This project uses the following dependencies:

- iText PDF (version 7.0.4)
- Apache Commons Codec (version 1.9)
- Apache Argparse4j (version 0.8.1)

## License

This project is licensed under the [GNU Affero General Public License (AGPL)](https://www.gnu.org/licenses/agpl-3.0.en.html).
