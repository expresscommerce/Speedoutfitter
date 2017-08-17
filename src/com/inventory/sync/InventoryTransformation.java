package com.inventory.sync;

import com.utils.ItemMasterDetail;
import com.utils.Property;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.io.File;

/**
 * Created by Islam-uddin on 4/10/2017.
 */
public class InventoryTransformation {
    private static Logger logger = LoggerFactory.getLogger(InventoryTransformation.class);

    public static String fileName = "";

    /*   TODO   2_Vendor Part# - master tr->    item[0]brand[skip:no:wo]
         TODO   3_VND_ITEM# - wps->             VND_ITEM/part[9]brand[10]
         TODO   4_VendorPunctutdPart#-          pu x[3]brand[25]
         TODO   7_itemmaster_fixed               [2][6][sIndex:0]
         ----------------------------------------------------------------
         TODO   0.csv	                        ["x"][7][10]
         TODO   1_MN_INV.csv		            [","][0][1]
         TODO   2_invupd_tr.txt		            [","][0][2,3,4,5,6,7,8]
         TODO   3_WPS_Daily_Inv.csv	            ["|"][0][2,3,4,5,6,7]
         TODO   4_BAIx_DlrPrice_pu.csv	        [","][1][13,14,15,16,17,18]
         TODO   5_INVENTORY_bell.txt            ["|"][4][5]
         TODO   6_QBinven.xlsx		            ["x"][0][4][sIndex:1]
         TODO   7_itmmastr_041417.csv	        [","][15][3]
         ----------------------------------------------------------------*/

    public static String csvFile = "F:\\inventory sync\\input\\0.csv";
    public static String csvVendor_1_MN_INV = "F:\\inventory sync\\input\\1.csv";
    public static String csvVendor_2_invupd_tr = "F:\\inventory sync\\input\\2.txt";
    public static String csvVendor_3_WPS_Daily_Inventory = "F:\\inventory sync\\input\\3.csv";
    public static String csvVendor_4_BAI026_DealerPrice_pu = "F:\\inventory sync\\input\\4.csv";
    public static String csvVendor_5_INVENTORY_bell = "F:\\inventory sync\\input\\5.txt";
    public static String csvVendor_6_QBinven = "F:\\inventory sync\\input\\6.xlsx";
    public static String csvVendor_7_itemMasterOfDetail = "F:\\inventory sync\\input\\7.csv";
    public static String csvSingleFile = "F:\\inventory sync\\export\\FileExchange_Response_38043182_single.csv";
    public static String csvMultipleFile = "F:\\inventory sync\\export\\FileExchange_Response_38043182_multiple.csv";
    private static final String LOG_TEXTFILE = "F:\\inventory sync\\log\\log_";
    private static final String LOG_TEXTFILE_DIR = "F:\\inventory sync\\log";
    public static String csvVendor_7_itemMasterFixed = LOG_TEXTFILE_DIR+"\\7_itemmaster_fixed.xlsx";
    private static final boolean LOG_TO_FILE=true;
    static Map<String, ItemMasterDetail> dataMapTR = new HashMap<String, ItemMasterDetail>();
    static Map<String, ItemMasterDetail> dataMapWPS = new HashMap<String, ItemMasterDetail>();
    static Map<String, ItemMasterDetail> dataMapPU = new HashMap<String, ItemMasterDetail>();

    public static Map<String, ItemMasterDetail> itemMasterDataMap = new HashMap<String, ItemMasterDetail>();
    public static Map<String, String> itemMasterDataMap_forTR = new HashMap<String, String>();

    static public void logToFile(String fileName, String content) {
        if(LOG_TO_FILE) {
            BufferedWriter bw = null;
            FileWriter fw = null;
            try {
                fw = new FileWriter(LOG_TEXTFILE + fileName, true);
                bw = new BufferedWriter(fw);
                bw.write(content);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (bw != null)
                        bw.close();
                    if (fw != null)
                        fw.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    static public void main(String[] args) throws XmlException {

        if(LOG_TO_FILE){
            for(File f: new File(LOG_TEXTFILE_DIR).listFiles()){
                f.delete();
            }
        }

        itemMasterTRinit();
        InventoryTransformation invDataTest = new InventoryTransformation();
        try {
            Map<String, Integer> dataMap1 = new HashMap<String, Integer>();
            Map<String, Integer> dataMap2 = new HashMap<String, Integer>();
            Map<String, Integer> dataMap3 = new HashMap<String, Integer>();
            Map<String, Integer> dataMap4 = new HashMap<String, Integer>();
            Map<String, Integer> dataMap5 = new HashMap<String, Integer>();
            Map<String, Integer> dataMap6 = new HashMap<String, Integer>();

            Map<String, Integer> masterDataMap = new HashMap<String, Integer>();
            dataMap1 = readVendorsData(false, false, csvVendor_1_MN_INV, ",", 0, "no", 1);
            dataMap2 = readVendorsData(false, false, csvVendor_2_invupd_tr, ",", 0, "no", 2, 3, 4, 5, 6, 7, 8);
            dataMap3 = readVendorsData(false, false, csvVendor_3_WPS_Daily_Inventory, "|", 0, "yes", 2, 3, 4, 5, 6, 7);//fahad ne yahan par 9 index use ki he
            dataMap4 = readVendorsData(false, false, csvVendor_4_BAI026_DealerPrice_pu, ",,", 1, "yes", 13, 14, 15, 16, 17, 18);
            dataMap5 = readVendorsData(false, false, csvVendor_5_INVENTORY_bell, "|", 4, "yes", 5);
            dataMap6 = readVendorsExcelData(false, false, csvVendor_6_QBinven, 1, 0, "yes", 4);

            ArrayList<String> duplicatePartsList = new ArrayList<String>();
            // TODO 1 : First map : add operation
            // TODO 2 : All others maps : merge operations
            Set<String> keys1 = dataMap1.keySet();
            for (String key : keys1) {
                masterDataMap.put(key, dataMap1.get(key));
            }
            mergedDataMaps(dataMap2, masterDataMap, duplicatePartsList);
            mergedDataMaps(dataMap3, masterDataMap, duplicatePartsList);
            mergedDataMaps(dataMap4, masterDataMap, duplicatePartsList);
            mergedDataMaps(dataMap5, masterDataMap, duplicatePartsList);
            mergedDataMaps(dataMap6, masterDataMap, duplicatePartsList);
            logger.info("duplicatePartsList :" + duplicatePartsList.size());
            logger.info("total scanned  records (1+2+3+4+5+6=Master Total):" + (dataMap1.size() + dataMap2.size() + dataMap3.size() + dataMap4.size() + dataMap5.size() + dataMap6.size()) + "=" + masterDataMap.size() + "");
            Map<String, Property> singlePartsMap = invDataTest.readMasterFile(csvFile, masterDataMap);
        } catch (Exception e) {
            logger.info("Error! Exception caught");
            e.printStackTrace();
        }
    }

    public static Map<String, ItemMasterDetail> readVendorsData1(boolean isLog, boolean isSummary, Set<String> brandsFilterList, String vendor_File,
                                                                 String delimiter,
                                                                 int part_num_index,
                                                                 String skipFirstRow,
                                                                 Integer... qty_index) {
        String line = "";
        String cvsSplitBy = ",";
        Map<String, ItemMasterDetail> partsMap = new HashMap<String, ItemMasterDetail>();
        int row = 1;
        String rowData = "";

        try (BufferedReader vbr = new BufferedReader(new InputStreamReader(new FileInputStream(vendor_File), "ISO-8859-1"))) {
            String header = "";
            if (skipFirstRow.equalsIgnoreCase("yes")) header = vbr.readLine();
            File file = new File(vendor_File);

            Set<String> duplicateValidationList = new HashSet<String>();
            ArrayList<String> duplicateFoundList = new ArrayList<String>();

            while ((line = vbr.readLine()) != null) {
                ItemMasterDetail itemMasterDetail = new ItemMasterDetail();

                rowData = "";
                if (!line.trim().isEmpty()) {
                    String[] columns = null;
                    String[] columns2 = null;
                    if (delimiter.equals("|")) {
                        columns = line.split("\\|");
                    } else if (delimiter.equals("\t")) {
                        columns = line.split("\t");
                    } else if (delimiter.equals(",")) {
                        columns = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                    } else if (delimiter.equals(",,")) {
                        columns = line.split(",");
                    }
                    String vendorPartNo = columns[part_num_index].trim();

                    rowData = "[" + row + "] PART#:[" + vendorPartNo + "]";
                    boolean go = true;
                    if (!vendorPartNo.isEmpty()) {
                        Integer totalQty = 0;
                        for (Integer index : qty_index) {
                            if (columns[index].equals("+")) {
                                columns[index] = "10";
                            }
                            rowData += "[" + index + ":" + columns[index] + "],";
                            if (columns[index].trim().isEmpty()) {
                                columns[index] = columns[index];
                            } else {
                                totalQty += Integer.parseInt(columns[index]);
                            }
                        }
                        rowData += " = " + totalQty;
                        itemMasterDetail.setVendorPartNumber(vendorPartNo);

                        if (vendor_File.equals(csvVendor_2_invupd_tr)) {
                            if (totalQty >= 0) {
                                itemMasterDetail.setTrQty(totalQty + "");
                            } else {
                                itemMasterDetail.setTrQty("");
                            }
                            if (columns[0] != null && columns[0] != null) itemMasterDetail.setItem(columns[0]); //[ok]
                            else itemMasterDetail.setItem("");

                            itemMasterDetail.setBrand("skip");  //has no brand
                        }

                        if (vendor_File.equals(csvVendor_3_WPS_Daily_Inventory)) {
                            itemMasterDetail.setWpsQty(totalQty + "");
                            if (columns[0] != null && columns[0] != null) itemMasterDetail.setItem(columns[0]);   //[ok]
                            else itemMasterDetail.setItem("");

                            if (columns[10] != null && columns[10] != null) itemMasterDetail.setBrand(columns[10]);
                            else itemMasterDetail.setBrand("");
                        }
                        if (vendor_File.equals(csvVendor_4_BAI026_DealerPrice_pu)) {
                            itemMasterDetail.setPuQty(totalQty + "");
                            if (columns[1] != null && columns[1] != null) itemMasterDetail.setItem(columns[0]);//[ok]
                            else itemMasterDetail.setItem("");

                            if (columns[25] != null && columns[25] != null) itemMasterDetail.setBrand(columns[25]);
                            else itemMasterDetail.setBrand("");
                        }

                        if (brandsFilterList.contains(itemMasterDetail.getBrand())) {
                            if (partsMap.get(vendorPartNo) != null) {
                                ItemMasterDetail itemMasterDetail2 = partsMap.get(vendorPartNo);


                                if (vendor_File.equals(csvVendor_2_invupd_tr)) {
                                    String trQty = (Integer.valueOf(itemMasterDetail2.getTrQty()) + Integer.valueOf(itemMasterDetail.getTrQty())) + "";
                                    itemMasterDetail.setWpsQty(trQty);

                                }
                                if (vendor_File.equals(csvVendor_3_WPS_Daily_Inventory)) {
                                    String wpsQty = (Integer.valueOf(itemMasterDetail2.getWpsQty()) + Integer.valueOf(itemMasterDetail.getWpsQty())) + "";
                                    itemMasterDetail.setWpsQty(wpsQty);
                                }
                                if (vendor_File.equals(csvVendor_4_BAI026_DealerPrice_pu)) {
                                    String puQty = (Integer.valueOf(itemMasterDetail2.getPuQty()) + Integer.valueOf(itemMasterDetail.getPuQty())) + "";
                                    itemMasterDetail.setWpsQty(puQty);
                                }

                                String prevQty = partsMap.get(vendorPartNo).getPuQty() + "," + partsMap.get(vendorPartNo).getWpsQty() + "," + partsMap.get(vendorPartNo).getTrQty();
                                String newQty = itemMasterDetail.getPuQty() + "," + itemMasterDetail.getWpsQty() + "," + itemMasterDetail.getTrQty();
                                ;
                                System.out.println(vendorPartNo + " :(prev:new) [" + prevQty + "][" + newQty + "]");
                                partsMap.replace(vendorPartNo, itemMasterDetail);
                                // old new summary
                            } else {
                                partsMap.put(vendorPartNo, itemMasterDetail);
                            }

                            duplicateValidationList.add(vendorPartNo);
                            if (duplicateValidationList.contains(vendorPartNo)) {
                                duplicateFoundList.add(vendorPartNo + ":" + itemMasterDetail.getBrand());
                            }
                        }
                    }
                }//empty check
                else {
                    rowData = " blank row";
                }
                row++;
                if (isLog) logger.info(rowData);
            }
            if (duplicateFoundList.size() > 0) {
                Collections.sort(duplicateFoundList);

                System.err.println(duplicateFoundList.size() + " duplicates found in " + vendor_File + " print : " + duplicateFoundList.toString());
                for (String obj : duplicateFoundList) {
                    //  System.err.println(obj); //on it for testing
                }
            }
        } catch (Exception e) {
            if (isLog) logger.info(rowData);
            e.printStackTrace();
        }

        if (isSummary) vendorNameHere_DataSummary("file path: " + vendor_File,
                "total records: " + partsMap.size());

        return partsMap;
    }


    static public void itemMasterTRinit() throws XmlException {

        String[] brandsArray = null;
        String brands = "K&N|TBR|K & N|K AND N ENGINEERING|TWO BROS|Two Brothers Offroad|Two Brothers Racing|skip";
        Set<String> brandsFilterList = getBrandsFilters(brands);
        itemMasterDataMap = readVendorsDataMaster(false, false, brandsFilterList, csvVendor_7_itemMasterOfDetail, ",", 15, "no", 3);

        dataMapTR = readVendorsData1(false, false, brandsFilterList, csvVendor_2_invupd_tr, ",", 0, "no", 2, 3, 4, 5, 6, 7, 8);
        System.out.println("dataMapTR.size():" + dataMapTR.size());
        dataMapWPS = readVendorsData1(false, false, brandsFilterList, csvVendor_3_WPS_Daily_Inventory, "|", 9, "yes", 2, 3, 4, 5, 6, 7);
        System.out.println("dataMapTR.size():" + dataMapWPS.size());
        dataMapPU = readVendorsData1(false, false, brandsFilterList, csvVendor_4_BAI026_DealerPrice_pu, ",,", 3, "yes", 13, 14, 15, 16, 17, 18);
        System.out.println("dataMapTR.size():" + dataMapPU.size());
        ArrayList<ItemMasterDetail> itemMasterList = new ArrayList<ItemMasterDetail>();
        try {
            //write to excel:
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("itemMasterDetail");

            int rowCount = 0;
            Set<String> keys = itemMasterDataMap.keySet();
            Set<String> keysTR1 = dataMapTR.keySet();
            Set<String> keysWPS1 = dataMapWPS.keySet();
            Set<String> keysPU1 = dataMapPU.keySet();
            System.out.println("before keysTR:" + keysTR1.size());
            System.out.println("before keysWPS:" + keysWPS1.size());
            System.out.println("before keysPU:" + keysPU1.size());
            Set<String> itemList = new HashSet<String>();
            for (String key : keys) {
                ItemMasterDetail writeObj = new ItemMasterDetail();
                ItemMasterDetail itemMasterDetailM = itemMasterDataMap.get(key);
                writeObj = itemMasterDetailM;
                itemList.add(itemMasterDetailM.getItem());

                //tr
                if (dataMapTR.get(itemMasterDetailM.getItem()) != null) {
                    ItemMasterDetail itemMasterDetailD = dataMapTR.get(itemMasterDetailM.getItem());
                    dataMapTR.remove(itemMasterDetailM.getItem());
                    if (brandsFilterList.contains(itemMasterDetailD.getBrand())) {
                        writeObj.setTrQty(itemMasterDetailD.getTrQty());
                    } else writeObj.setTrQty("contraductive Brand : " + itemMasterDetailD.getBrand() + "");
                } else writeObj.setTrQty("");

                //wps
                if (dataMapWPS.get(itemMasterDetailM.getVendorPartNumber()) != null) {
                    ItemMasterDetail itemMasterDetailD = dataMapWPS.get(itemMasterDetailM.getVendorPartNumber());
                    dataMapWPS.remove(itemMasterDetailM.getVendorPartNumber());
                    if (brandsFilterList.contains(itemMasterDetailD.getBrand())) {
                        writeObj.setWpsQty(itemMasterDetailD.getWpsQty());
                    } else writeObj.setWpsQty("contraductive Brand : " + itemMasterDetailD.getBrand() + "");
                } else writeObj.setWpsQty("");

                //wps
                if (dataMapPU.get(itemMasterDetailM.getVendorPartNumber()) != null) {
                    ItemMasterDetail itemMasterDetailD = dataMapPU.get(itemMasterDetailM.getVendorPartNumber());
                    dataMapPU.remove(itemMasterDetailM.getVendorPartNumber());
                    if (brandsFilterList.contains(itemMasterDetailD.getBrand())) {
                        writeObj.setPuQty(itemMasterDetailD.getPuQty());
                    } else writeObj.setPuQty("contraductive Brand : " + itemMasterDetailD.getBrand() + "");
                } else writeObj.setPuQty("");
                writeObj.getVendorPartNumber();

                Integer qty = null;
                qty = (writeObj.getTrQty() != null && !writeObj.getTrQty().isEmpty()) ? Integer.valueOf(writeObj.getTrQty()) : 0;
                qty += (writeObj.getWpsQty() != null && !writeObj.getWpsQty().isEmpty()) ? Integer.valueOf(writeObj.getWpsQty()) : 0;
                qty += (writeObj.getPuQty() != null && !writeObj.getPuQty().isEmpty()) ? Integer.valueOf(writeObj.getPuQty()) : 0;

                writeObj.setQtySum(qty);

                rowCount = addSheetRow(sheet, rowCount, writeObj);
                itemMasterList.add(writeObj);
            }

            Set<String> keysTR = dataMapTR.keySet();
            Set<String> keysWPS = dataMapWPS.keySet();
            Set<String> keysPU = dataMapPU.keySet();
            System.out.println("after keysTR:" + keysTR.size());
            System.out.println("after keysWPS:" + keysWPS.size());
            System.out.println("after keysPU:" + keysPU.size());

            Map<String, String> data = readMasterFileForItemTR();
            for (String key : keysWPS) {

                if (key.equals("005-2630406V") && data.get("005-2630406V") != null) {
                    data.get("005-2630406V");
                }
                data.get("005-2630406V");

                if (data.get(key) != null) {
                    ItemMasterDetail writeObj = dataMapWPS.get(key);
                    Integer qty = null;
                    qty = (writeObj.getTrQty() != null && !writeObj.getTrQty().isEmpty()) ? Integer.valueOf(writeObj.getTrQty()) : 0;
                    qty += (writeObj.getWpsQty() != null && !writeObj.getWpsQty().isEmpty()) ? Integer.valueOf(writeObj.getWpsQty()) : 0;
                    qty += (writeObj.getPuQty() != null && !writeObj.getPuQty().isEmpty()) ? Integer.valueOf(writeObj.getPuQty()) : 0;

                    writeObj.setQtySum(qty);

                    rowCount = addSheetRow(sheet, rowCount, writeObj);
                }
            }
            for (String key : keysPU) {
                if (data.get(key) != null) {
                    ItemMasterDetail writeObj = dataMapPU.get(key);
                    Integer qty = null;
                    qty = (writeObj.getTrQty() != null && !writeObj.getTrQty().isEmpty()) ? Integer.valueOf(writeObj.getTrQty()) : 0;
                    qty += (writeObj.getWpsQty() != null && !writeObj.getWpsQty().isEmpty()) ? Integer.valueOf(writeObj.getWpsQty()) : 0;
                    qty += (writeObj.getPuQty() != null && !writeObj.getPuQty().isEmpty()) ? Integer.valueOf(writeObj.getPuQty()) : 0;

                    writeObj.setQtySum(qty);

                    rowCount = addSheetRow(sheet, rowCount, writeObj);
                }
            }

            try (FileOutputStream outputStream = new FileOutputStream(csvVendor_7_itemMasterFixed)) {
                workbook.write(outputStream);
            }

        } catch (IOException io) {
            io.printStackTrace();
        }

        System.out.println("itemMasterList: " + itemMasterList.size());

        System.out.println(itemMasterDataMap.size() + "," + brandsFilterList.size());

    }

    private static int addSheetRow(XSSFSheet sheetWPS, int rowCount, ItemMasterDetail writeObj) {
        Row row = sheetWPS.createRow(rowCount++);
        int columnCount = 0;
        for (int i = 0; i < 7; i++) {
            Cell cell = row.createCell(columnCount++);
            Object field = null;
            if (i == 0) field = writeObj.getItem();
            if (i == 1) field = writeObj.getBrand();
            if (i == 2) field = writeObj.getVendorPartNumber();
            if (i == 3) field = writeObj.getTrQty();
            if (i == 4) field = writeObj.getWpsQty();
            if (i == 5) field = writeObj.getPuQty();
            if (i == 6) field = writeObj.getQtySum();

            if (field instanceof String) {
                cell.setCellValue((String) field);
            } else if (field instanceof Integer) {
                cell.setCellValue((Integer) field);
            }
        }
        return rowCount;
    }

    private static void mergedDataMaps(Map<String, Integer> dataMap2, Map<String, Integer> masterDataMap, ArrayList duplicatePartsList) {
        Set<String> keys2 = dataMap2.keySet();
        for (String key : keys2) {
            if (masterDataMap.get(key) == null) {
                masterDataMap.put(key.trim(), dataMap2.get(key));
            } else {
                masterDataMap.put(key.trim(), (dataMap2.get(key) + masterDataMap.get(key)));
                duplicatePartsList.add(key);
            }
        }
    }

    private static HashMap<String, Integer> readItemMasterDetail(
            boolean isLog, boolean isSummary, String vendor_File, int sheetIndex,
            int part_num_index,
            String skipFirstRow,
            Integer... qty_index
    ) {
        HashMap<String, Integer> dataMap = new HashMap<String, Integer>();
        try {
            FileInputStream inputStream = new FileInputStream(new File(vendor_File));
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet firstSheet = workbook.getSheetAt(sheetIndex);
            Iterator<Row> iterator = firstSheet.iterator();

            String key = "";
            Integer value = 0;
            int row = 1;
            String rowData = "";

            String header = "";
            while (iterator.hasNext()) {
                rowData = "";
                if (skipFirstRow.equalsIgnoreCase("yes")) {
                    //its the header so skip it

                    if (row == 1) {
                        Row nextRow = iterator.next();
                    }
                }

                Row nextRow = iterator.next();
                Iterator<Cell> cellIterator = nextRow.cellIterator();
                int columns = 0;
                value = 0;
                key = "";
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    if (columns == part_num_index) {
                        key = cell.getStringCellValue();
                    }

                    Integer totalQty = 0;
                    for (Integer index : qty_index) {
                        if (columns == index) {
                            value += Double.valueOf(cell.getNumericCellValue()).intValue();
                            if (dataMap.get(key) != null) value += dataMap.get(key);
                            if (value.equals("+")) value = 10;
                            if (value < 0) value = 0;
                        }
                    }

                    if (!key.isEmpty() && value != null) {
                        rowData = "[" + row + "] PART#:[" + key + "][" + columns + ":" + value + "],= " + value;
                        dataMap.put(key, value);
                    }
                    columns++;
                }
                row++;
                if (isLog == true) {
                    logger.info(rowData);
                }
            }
            // workbook.close();
            inputStream.close();

            //  return quantity;
            if (isSummary == true) {
                vendorNameHere_DataSummary("file path: " + vendor_File, "total records: " + dataMap.size());
            }

        } catch (FileNotFoundException f) {
            //handle file not found
            logger.info("Error! file not found caught ");
            f.printStackTrace();
        } catch (IOException io) {
            //handle io exception
            logger.info("Error! loadWorkBook failed ");
            io.printStackTrace();
        }

        return dataMap;
    }

    private static HashMap<String, Integer> readVendorsExcelData(
            boolean isLog, boolean isSummary, String vendor_File, int sheetIndex,
            int part_num_index,
            String skipFirstRow,
            Integer... qty_index
    ) {

        HashMap<String, Integer> dataMap = new HashMap<String, Integer>();
        try {
            FileInputStream inputStream = new FileInputStream(new File(vendor_File));
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet firstSheet = workbook.getSheetAt(sheetIndex);
            Iterator<Row> iterator = firstSheet.iterator();

            String key = "";
            Integer value = 0;
            int row = 1;
            String rowData = "";

            String header = "";
            while (iterator.hasNext()) {
                rowData = "";
                if (skipFirstRow.equalsIgnoreCase("yes")) {
                    //its the header so skip it

                    if (row == 1) {
                        Row nextRow = iterator.next();
                    }
                }

                Row nextRow = iterator.next();
                Iterator<Cell> cellIterator = nextRow.cellIterator();
                int columns = 0;
                value = 0;
                key = "";
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    if (columns == part_num_index) {
                        key = cell.getStringCellValue();
                        if (key.equals("491282")) {
                            System.out.println("key :" + key);
                        }
                    }

                    Integer totalQty = 0;
                    for (Integer index : qty_index) {
                        if (columns == index) {
                            value += Double.valueOf(cell.getNumericCellValue()).intValue();
                            if (dataMap.get(key) != null) value += dataMap.get(key);
                            if (value.equals("+")) value = 10;
                            if (value < 0) value = 0;
                        }
                    }


                    if (!key.isEmpty() && value != null) {
                        if (key.equals("491282")) {
                            System.out.println("key :" + key);
                        }
                        rowData = "[" + row + "] PART#:[" + key + "][" + columns + ":" + value + "],= " + value;
                        dataMap.put(key.toUpperCase(), value);
                    }
                    columns++;
                }
                row++;
                if (isLog == true) {
                    logger.info(rowData);
                }

            }
            // workbook.close();
            inputStream.close();

            //  return quantity;
            if (isSummary == true) {
                vendorNameHere_DataSummary("file path: " + vendor_File, "total records: " + dataMap.size());
            }

        } catch (FileNotFoundException f) {
            //handle file not found
            logger.info("Error! file not found caught ");
            f.printStackTrace();

        } catch (IOException io) {
            //handle io exception
            logger.info("Error! loadWorkBook failed ");
            io.printStackTrace();
        }
        return dataMap;
    }


    public static Map<String, ItemMasterDetail> readVendorsDataMaster(boolean isLog, boolean isSummary, Set<String> brandsFilterList, String vendor_File,
                                                                      String delimiter,
                                                                      int part_num_index,
                                                                      String skipFirstRow,
                                                                      Integer... qty_index) {
        String line = "";
        String cvsSplitBy = ",";
        Map<String, ItemMasterDetail> partsMap = new HashMap<String, ItemMasterDetail>();
        int row = 1;
        String rowData = "";

        try (BufferedReader vbr = new BufferedReader(new InputStreamReader(new FileInputStream(vendor_File), "ISO-8859-1"))) {
            String header = "";
            if (skipFirstRow.equalsIgnoreCase("yes")) header = vbr.readLine();
            Set<String> usedBrandsSet = new HashSet<String>();
            Set<String> foundBrandsSet = new HashSet<String>();
            Set<String> duplicateBrands = new HashSet<String>();
            int foundRows = 0;
            ArrayList<String> duplicateValidationList = new ArrayList<String>();
            Set<Integer> columnsSizeList = new HashSet<Integer>();


            while ((line = vbr.readLine()) != null) {
                ItemMasterDetail itemMasterDetail = new ItemMasterDetail();
                rowData = "";
                if (!line.trim().isEmpty()) {
                    String[] columns = getLineToColumns(delimiter, line);
                    columnsSizeList.add(columns.length);


                    String brand = columns[qty_index[0]].trim().toUpperCase();
                    String vendorPartNumber = columns[part_num_index].trim();
                    String item = columns[0].trim();

                    usedBrandsSet.add(brand);
                    if (!vendorPartNumber.isEmpty() && brandsFilterList.contains(brand)) {
                        itemMasterDetail.setBrand(brand);
                        itemMasterDetail.setVendorPartNumber(vendorPartNumber);
                        itemMasterDetail.setItem(item);

                        foundRows++;
                        foundBrandsSet.add(brand);
                        rowData = "[" + row + "] PART#:[" + vendorPartNumber + "]";
                        rowData += " = brand: " + brand;
                        partsMap.put(item, itemMasterDetail);
                        if (duplicateValidationList.contains(vendorPartNumber)) {
                            duplicateBrands.add(vendorPartNumber);
                            System.out.println("duplicate part# : " + vendorPartNumber);
                        }
                        duplicateValidationList.add(vendorPartNumber);
                        itemMasterDataMap_forTR.put(columns[0].trim(), vendorPartNumber);
                        if (isLog) logger.info(rowData);
                    }
                }//empty check
                else {
                    rowData = " blank row";
                    if (isLog) logger.info(rowData);
                }
                row++;

            }
            System.out.println("foundRows : " + foundRows);
            if (Collections.max(columnsSizeList) != 17)
                System.err.println("max col len:" + Collections.max(columnsSizeList));
            if (Collections.min(columnsSizeList) != 17)
                System.err.println("min col len:" + Collections.max(columnsSizeList));
            for (String brand : usedBrandsSet) {
                //  System.out.println(brand);
            }
            System.out.println("========================");
            for (String brand : foundBrandsSet) {
                System.out.println(brand);
            }
        } catch (Exception e) {
            if (isLog) logger.info(rowData);
            e.printStackTrace();
        }

        if (isSummary) vendorNameHere_DataSummary("file path: " + vendor_File,
                "total records: " + partsMap.size());

        return partsMap;
    }

    private static String[] getLineToColumns(String delimiter, String line) {
        String[] columns = null;
        if (delimiter.equals("|")) {
            columns = line.split("\\|");
        } else if (delimiter.equals("\t")) {
            columns = line.split("\t");
        } else if (delimiter.equals(",")) {
            columns = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        } else if (delimiter.equals(",,")) {
            columns = line.split(",");
        }
        return columns;
    }

    private static Set<String> getBrandsFilters(String brands) {
        String[] brandsArray;
        brandsArray = brands.split("\\|");

        Set<String> brandsFilterList = new HashSet<String>();
        for (int arr = 0; arr < brandsArray.length; arr++) {
            brandsFilterList.add(brandsArray[arr]);
        }
        return brandsFilterList;
    }


    public static Map<String, Integer> readVendorsData(boolean isLog, boolean isSummary, String vendor_File,
                                                       String delimiter,
                                                       int part_num_index,
                                                       String skipFirstRow,
                                                       Integer... qty_index) {
        String line = "";
        String cvsSplitBy = ",";
        Map<String, Integer> partsMap = new HashMap<String, Integer>();
        int row = 1;
        String rowData = "";

        try (BufferedReader vbr = new BufferedReader(new InputStreamReader(new FileInputStream(vendor_File), "ISO-8859-1"))) {
            String header = "";
            if (skipFirstRow.equalsIgnoreCase("yes")) header = vbr.readLine();

            while ((line = vbr.readLine()) != null) {
                rowData = "";
                if (!line.trim().isEmpty()) {
                    String[] columns = null;
                    if (delimiter.equals("|")) {
                        columns = line.split("\\|");

                    } else if (delimiter.equals("\t")) {
                        columns = line.split("\t");
                    } else if (delimiter.equals(",")) {
                        columns = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                    } else if (delimiter.equals(",,")) {
                        columns = line.split(",");
                    }
                    rowData = "[" + row + "] PART#:[" + columns[part_num_index].trim() + "]";
                    boolean go = true;
                    if (!columns[part_num_index].trim().isEmpty() && go) {
                        Integer totalQty = 0;
                        for (Integer index : qty_index) {
                            if (columns[index].equals("+")) columns[index] = "10";
                            rowData += "[" + index + ":" + columns[index] + "],";
                            totalQty += Integer.parseInt(columns[index]);
                        }
                        rowData += " = " + totalQty;
                        partsMap.put(columns[part_num_index].trim(), (totalQty < 0) ? 0 : totalQty);
                        logToFile(new File(vendor_File).getName(), printBlockStr(columns[part_num_index].trim(), ((totalQty < 0) ? 0 : totalQty) + ""));
                    }
                }//empty check
                else {
                    rowData = " blank row";
                }
                row++;
                if (isLog) logger.info(rowData);
            }
        } catch (Exception e) {
            if (isLog) logger.info(rowData);
            e.printStackTrace();
        }

        if (isSummary) vendorNameHere_DataSummary("file path: " + vendor_File,
                "total records: " + partsMap.size());

        return partsMap;
    }


    private static Map<String, String> readMasterFileForItemTR() {
        Map<String, String> data = new HashMap<String, String>();

        String line = "";
        String cvsSplitBy = ",";
        int row = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile), "ISO-8859-1"))) {
            String header = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] columns = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);//line.split(cvsSplitBy);

                String qty = "0";
                if (columns[7] != null) qty = columns[7];

                String partNum = "";
                if (columns.length > 10 && columns[10] != null && !columns[10].isEmpty() && !columns[10].trim().equals("")) {
                    partNum = columns[10];
                    data.put(partNum, qty);
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }


    private Map<String, Property> readMasterFile(String master_File, Map<String, Integer> masterDataMap) {
        String line = "";
        String cvsSplitBy = ",";
        int row = 0;
        Map<String, Property> partsMap = new HashMap<String, Property>();

        Map<String, Integer> dataMapItemMasterTR = new HashMap<String, Integer>();

        Map<String, Integer> dataMap6 = new HashMap<String, Integer>();
        //QB FILE READ AGAIN
        dataMap6 = readVendorsExcelData(false, false, csvVendor_6_QBinven, 1, 0, "yes", 4);
        dataMapItemMasterTR = readItemMasterDetail(false, false, csvVendor_7_itemMasterFixed, 0, 2, "no", 6);

        int beforeSizeDataMapItemMasterTR = dataMapItemMasterTR.size();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(master_File), "ISO-8859-1"))) {
            String header = br.readLine();
            boolean groupStart = false;
            File singleFile = new File(csvSingleFile);
            File multipleFile = new File(csvMultipleFile);

            if (!singleFile.exists()) {
                singleFile.createNewFile();
            }

            if (!multipleFile.exists()) {
                multipleFile.createNewFile();
            }

            FileWriter sfw = new FileWriter(singleFile.getAbsoluteFile());
            FileWriter mfw = new FileWriter(multipleFile.getAbsoluteFile());
            BufferedWriter sbw = new BufferedWriter(sfw);
            BufferedWriter mbw = new BufferedWriter(mfw);
            //// TODO: 4/13/2017  HEADER ME KUCH JAENGE
            sbw.write(getLineData(header.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1)));
            mbw.write(getLineData(header.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1)));


            boolean groupLeader = false;

            while ((line = br.readLine()) != null) {
                row++;
                String[] columns1 = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);//line.split(cvsSplitBy);
                String[] columns = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);//line.split(cvsSplitBy);
                if (groupStart == true && !columns[0].isEmpty()) groupStart = false;

                if (columns.length == 10) {
                    List<String> values = new ArrayList<String>();
                    for (int i = 0; i < columns.length; i++) values.add(String.valueOf(columns[i]));
                    values.add("");
                    columns = values.toArray(new String[values.size()]);
                }
                if (columns[10].isEmpty()) {
                    groupStart = true;
                    groupLeader = true;
                }
                Integer currentQty = 0;
                if (columns[10].startsWith("\"")) {
                    columns[10] = columns[10].substring(1, columns[10].length() - 1);
                }


                if (masterDataMap.get(columns[10].trim()) != null) currentQty = masterDataMap.get(columns[10].trim());
                else currentQty = null;

                Integer previousQty = 0;
                if (!columns[7].isEmpty()) previousQty = (Integer.parseInt(columns[7].trim()));

                Integer currentQty1 = (currentQty == null) ? 0 : currentQty;
                System.out.println("\"" + columns[10].trim() + "\",\"" + currentQty1 + "\",\"" + previousQty + "\",\"" + (currentQty1 - previousQty) + "\",\"" + ((!groupStart) ? "SINGLE" : "GROUP") + "\"");

                // ITEM MASTER DETAIL TR FILE CHECKING
                String partNo = columns[10].trim();
                Integer qtyWoTR=currentQty;
                Integer qtyTR=null;

                if (dataMapItemMasterTR.get(partNo) != null) {
                    currentQty = dataMapItemMasterTR.get(partNo);
                    //Qb Qty To Be Sum Here
                    if (dataMap6.get(partNo) != null && dataMap6.get(partNo) != 0) {
                        currentQty += dataMap6.get(partNo);
                        logToFile("from_QB.txt", printBlockStr(partNo));
                    }
                    qtyTR=currentQty;

                    dataMapItemMasterTR.remove(partNo);
                }
                //ENDS

                if (!columns[10].trim().isEmpty()) {
                    partsMap.put(columns[10].trim(), new Property(columns[10].trim(), previousQty, currentQty));
                    logToFile("partNum_previous_current_qty_comparison.txt", printBlockStr(columns[10].trim(), previousQty + "",currentQty+"",qtyWoTR+"",qtyTR+""));
                }

                if (!groupStart) {      //single
                    if (currentQty != null) {
                        Integer result = currentQty - previousQty;//0-4=-4
                        if (currentQty != null && previousQty != null && result != 0) { //"change" if result is not zero and add it to new file
                            columns[7] = currentQty + "";
                            //// TODO: 4/13/2017 export only specific columns
                            sbw.write(getLineData(columns));
                        }
                    } else {
                        Integer result = previousQty;//0-4=-4
                        if (result != null && result != 0) { //"change" if result is not zero and add it to new file
                            sbw.write(getLineData(columns));
                        }
                    }
                } else {               //multi file
                    if (columns != null) {
                        if (groupLeader)
                            groupLeader = false;
                        else
                            columns[7] = currentQty + "";

                        if (columns[7].isEmpty() || columns[7] == null || columns[7].equals("null")) {
                            //columns[7]="0";
                            columns[7] = previousQty + "";
                        }
                        mbw.write(getLineData(columns));
                    }
                }
            }
            sbw.close();
            mbw.close();
            logger.info("Done adding to single file...");

            int afterSizeDataMapItemMasterTR = dataMapItemMasterTR.size();
            int utilizedParts = beforeSizeDataMapItemMasterTR - afterSizeDataMapItemMasterTR;

            Set<String> imtKeys = dataMapItemMasterTR.keySet();

            for (String key : imtKeys) {
                logger.info("Remaining Part#: " + key);
            }


            logger.info("dataMapItemMasterTR (total part-utilizedParts=Remaining Parts): " + beforeSizeDataMapItemMasterTR + "-" + utilizedParts + "=" + (beforeSizeDataMapItemMasterTR - utilizedParts) + " or " + afterSizeDataMapItemMasterTR);

        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("Part# for Single File " + partsMap.size() + " loaded for parsing");

        return partsMap;
    }

    public String getLineData(String[] lineArr) {
        List<String> row = Arrays.asList(lineArr);
        return getRowData(row, ",");
    }

    public String getRowData(List<String> rows, String delimiter) {
        if (!rows.isEmpty()) {
            StringBuffer data = new StringBuffer();
            for (int i = 0; i < rows.size(); i++) {
                String row = rows.get(i);
                data.append(row);
                if (i < rows.size() - 1)
                    data.append(delimiter);
            }
            return data.append("\n").toString();
        }
        return "";
    }

    public static void vendorNameHere_DataSummary(String... summary) {
        logger.info("===================== DataSummary " + summary.length + " ====================");
        for (String s : summary) {
            logger.info(s);
        }
    }

    private static void printBlock(String... testDataResultSet) {
        int count = 0;
        System.out.print("[");
        for (String col : testDataResultSet) {
            count++;
            System.out.print(col);

            if (count != testDataResultSet.length) {
                System.out.print(" | ");
            } else {
                System.out.print("]");
            }
        }
//			System.out.print(+", ");
        System.out.println();
    }

    private static String printBlockStr(String... testDataResultSet) {
        String block="                    ";
        String fullBlock="";
        for (String col : testDataResultSet) ///word1 , word2
        {
            fullBlock+= col+block.substring(col.length(),block.length());;
        }
        fullBlock += "\n";
        return fullBlock;
    }

}
