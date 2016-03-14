<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:exslt="http://exslt.org/common" extension-element-prefixes="exslt" >
	<xsl:output method="html" indent="yes"/>
	<!-- Match the root element and create a templete -->
	<xsl:template match="Items">
		<html>
			<head>
				<!-- Insert stylesheet link -->
				<link href = "style.css" rel = "stylesheet" type = "text/css" />
				<title>Sales Amount By Provider</title>	
			</head>
			<body>
				<h1>Sales amount by provider</h1>
				<table class = "table">
					<!-- Create a group to loop through and group by (ProviderName) -->
					<xsl:for-each-group select = "Item" group-by = "ProviderName">
						<!-- Sort by ProviderName -->
						<xsl:sort select="current-grouping-key()" />
						<tr>
					 		<!-- Set the header of the table cell in the current group to the value of ProviderName -->
					 		<td colspan = "4" class = "headCell">Provider: <xsl:value-of select = "ProviderName" /></td>
					 	</tr>
					 	<tr>
						 	<td>Item Number</td>
							<td>Quantity</td>
							<td>Unit Price</td>
							<td>Total</td>
					 	</tr>	
		
					 	<!-- Begin another loop, within this ProviderName, looping through and grouping by ItemNumber -->
					 	<xsl:for-each-group select = "current-group()" group-by = "@ItemNumber">
								<!-- Sort by ItemNumber -->
								<xsl:sort select="current-grouping-key()" />
								<tr>
						 		<!-- Table cell with current value of ItemNumber -->
						 		<td><xsl:value-of select = "current-grouping-key()" /></td>
						 		<!-- Table cell with value of quantity of items for current item group in this provider group -->
						 		<td><xsl:value-of select = "sum(current-group()/Quantity)" /></td>
						 		<!-- Table cell with value of unit price for current item group in current provider groups -->
						 		<td><xsl:value-of select = "current-group()[1]/Price" /></td>
						 		<!-- Table cell with value of total price for current item group in current provider group -->
						 		<td><xsl:value-of select = "format-number(sum(current-group()/Quantity) * current-group()[1]/Price, '#.00')" /></td>
						 		</tr>
						</xsl:for-each-group>
						
						<!-- Create a new variable, storing sub total of price for current provider group -->
						<xsl:variable name = "subtotal">
						 		<xsl:value-of select ="format-number(sum(for $i in current-group() return $i/Price * $i/Quantity), '#.00')" />
						</xsl:variable>
						 <tr>
							<!-- Output the sub total variable to a table cell within each provider group -->
							<td align = "right" colspan = "3" class = "total">Sub-Total</td><td class = "total">$<xsl:value-of select = "$subtotal" /></td>
						</tr>
					</xsl:for-each-group>
					
					<!--  Both loops are now finished, item number groups and sub-totals have all been output -->
					<tr>
						<!-- Create a new loop for this variable, looping through each item, and totaling item price * item quantity, to give the grand total -->
						<xsl:variable name = "products">
							<xsl:for-each select = "Item">
								<node>
									<xsl:value-of select = "Quantity * Price" />
								</node>
							</xsl:for-each>
						</xsl:variable>
						<!--  Output the grand total once, after all previous loops are complete -->
						<td align = "right" class = "total" colspan = "3">Grand Total</td><td class = "total"><xsl:value-of select = "format-number(sum($products/node), '#.00')" /></td>
					</tr>
	
				</table>
			</body>
		</html>
	</xsl:template>
	
</xsl:stylesheet>