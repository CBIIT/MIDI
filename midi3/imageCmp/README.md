## Objective

Compare the pixel images of DICOM files before and after DICOM de-identification process to reveal what the changes are.
Python program run_DICOMImgCmp.py is used to compare the pixel image between before and after DICOM de-identification in a sigle thread process, and the comparision result is saved to a pdf file.
Python program run_DICOMImgCmp_multiT.py is used to compare the pixel image between before and after DICOM de-identification in multiple threads, and the comparision results are saved to multiple pdf files.

## Requirement 

To execute the above scripts, a ID mapping files in csv file format is required and its path should be specified in a configuration file passed to the programs as a execution parameter.
The configuration file must contain the following information, please refer to the template config_example_linux.json and config_win.json.

### Configuration file
| Configure             | Description                                           | Used by |
|-----------------------|-------------------------------------------------------|------------|
|"run_name"             | job name|both|
|"input_data_path"      | path to the folder containing the de-identified images|both|
|"output_data_path"     | path to the output|both|
|"uid_mapping_file"     | path to the UID mapping file|both|
|"log_path"             | path to the logs|both|
|"pre_deID_data_path" 	| path to the source data (pre de-identification)|both|
|"thread_count"         | The number of threads used to run the program |run_DICOMImgCmp_multiT.py| 


## Execution from the command line
To run  run_DICOMImgCmp.py, in the command line, enter
   ```python
   python run_DICOMImgCmp.py config_example_linux.json
   ```
To run_DICOMImgCmp_multiT.py, in the command line, enter
   ```python
   python run_DICOMImgCmp_multiT.py config_example_linux.json
   ```
