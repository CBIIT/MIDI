python program DICOMImgCmp.py compares all DICOM images in post-deidentified dataset with pre-deidentified dataset, assuming a ID mapping files is provided.

In command line, execute the following command 
python DICOMImgCmp.py <config.json>

config.json keeps the following parameters:
pre_processed_file_path -- the directory holds pre-deidentified dataset,
post_processed_file_path -- the directory holds post-deidentified dataset ,
id_map_csv_file -- the old ID and new ID mapping file path,
output_pdf_path -- the file path for comparison results if there is any discrepency found,
log_directory  -- the file path holds the execution log.