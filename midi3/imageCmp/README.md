## Objective

The image comparison program is designed to compare the pixel images of DICOM files before and after the de-identification process, revealing the changes made. Two versions of the program were developed: The Python script run_DICOMImgCmp.py compares pixel images before and after DICOM de-identification in a single-threaded process, saving the results to a PDF file. The Python script run_DICOMImgCmp_multiT.py performs the same comparison using a multi-threaded approach, with the results saved in multiple PDF files.

## Requirement 

Two datasets must be stored in the file system, and their paths should be specified in the configuration file, which is passed to the programs as an execution parameter. See the details of the configuration file in the section below. Additionally, to run the image comparison programs, a UID mapping file in CSV format must be provided in the configuration file

## UID Mapping File
The UID mapping file contains the old and new study, series, and instance UIDs for the two datasets. The mapping file uses the column headers, “id_old” (pre-de-identified UID) and “id_new” (post de-identified UID)

### Configuration file
The configuration file must contain the following information:

| Configure             | Description                                           | Used by |
|-----------------------|-------------------------------------------------------|------------|
|"run_name"             | job name|both|
|"input_data_path"      | path to the folder containing the de-identified DICOM images|both|
|"output_data_path"     | path to the output|both|
|"uid_mapping_file"     | path to the UID mapping file|both|
|"log_path"             | path to the logs|both|
|"pre_deID_data_path" 	| path to the source data (pre de-identification DICOM images)|both|
|"multiprocessing_cpus" | The number of threads used to run the program |run_DICOMImgCmp_multiT.py| 


## Execution from the command line
To run  run_DICOMImgCmp.py, in the command line, enter
   ```python
   python run_DICOMImgCmp.py config_example_linux.json
   ```
To run_DICOMImgCmp_multiT.py, in the command line, enter
   ```python
   python run_DICOMImgCmp_multiT.py config_example_linux.json
   ```
## Output
The image comparison programs will list all changed images by the de-identification process, showing the image before the de-identification process and after the de-identification process alone with the third image showing only the changed area, in a PDF file.
