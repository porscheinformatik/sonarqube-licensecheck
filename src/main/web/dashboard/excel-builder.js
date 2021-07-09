export default function buildExcel(dependencies, licenses) {
  let excelFile = `<?xml version="1.0" encoding="UTF-8"?><?mso-application progid="Excel.Sheet"?>
  <Workbook xmlns="urn:schemas-microsoft-com:office:spreadsheet"
    xmlns:x="urn:schemas-microsoft-com:office:excel"
    xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet"
    xmlns:html="http://www.w3.org/TR/REC-html40">`;
  excelFile += buildDependenciesWorksheet(dependencies);
  excelFile += buildLicensesWorksheet(licenses);
  excelFile += '</Workbook>';
  return excelFile;
}

function buildDependenciesWorksheet(dependencies) {
  let result = `<Worksheet ss:Name="Dependencies">
    <Table>
      <Row>
        <Cell>
          <Data ss:Type="String">Name</Data>
        </Cell>
        <Cell>
          <Data ss:Type="String">Version</Data>
        </Cell>
        <Cell>
          <Data ss:Type="String">License</Data>
        </Cell>
        <Cell>
          <Data ss:Type="String">Status</Data>
        </Cell>
      </Row>`;
  dependencies.forEach(dependency => {
    result += `<Row>
  <Cell><Data ss:Type="String">${dependency.name}</Data></Cell>
  <Cell><Data ss:Type="String">${dependency.version}</Data></Cell>
  <Cell><Data ss:Type="String">${dependency.license}</Data></Cell>
  <Cell><Data ss:Type="String">${dependency.status}</Data></Cell>
</Row>`;
  });
  return result + '</Table></Worksheet>';
}

function buildLicensesWorksheet(licenses) {
  let result = `<Worksheet ss:Name="Licenses">
    <Table>
      <Row>
        <Cell>
          <Data ss:Type="String">Identifier</Data>
        </Cell>
        <Cell>
          <Data ss:Type="String">Name</Data>
        </Cell>
        <Cell>
          <Data ss:Type="String">Status</Data>
        </Cell>
      </Row>`;
  licenses.forEach(license => {
    result += `<Row>
  <Cell><Data ss:Type="String">${license.identifier}</Data></Cell>
  <Cell><Data ss:Type="String">${license.name}</Data></Cell>
  <Cell><Data ss:Type="String">${license.status === 'true' ? 'Allowed' : 'Forbidden'}</Data></Cell>
</Row>`;
  });
  return result + '</Table></Worksheet>';
}
