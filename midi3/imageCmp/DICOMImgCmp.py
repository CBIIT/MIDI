import pydicom
import numpy as np
import matplotlib.pyplot as plt
from skimage.metrics import structural_similarity as ssim
from skimage import measure, morphology
from matplotlib.backends.backend_pdf import PdfPages
import csv
import os
import textwrap
import PyPDF2
import shutil
import json
import logging
import time
import argparse
 
# Using global debug flag
DEBUG_MODE = False

def load_dicom_image(file_path):
    """Load a DICOM file and return the image data as a numpy array."""
    try:
        dicom = pydicom.dcmread(file_path)
        # Check if Pixel Data is present
        if 'PixelData' not in dicom:
            return None
        image = dicom.pixel_array
        return image
    except AttributeError as e:
        print(f"Error loading DICOM image from {file_path}: {e}")
        return None
    except ValueError as e:
        print(f"ValueError loading DICOM image from {file_path}: {e}")
        return None

def compare_images(image1, image2):
    """Compare two images using Mean Squared Error and SSIM."""
    mse = np.mean((image1 - image2) ** 2)
    ssim_index, diff = ssim(image1, image2, full=True)
    return mse, ssim_index, diff


def find_changed_area(image1, image2, threshold=30):
    """Find the area of the image that has changed."""
    diff = np.abs(image1 - image2)
    mask = diff > threshold
    changed_area = np.sum(mask)
    return changed_area, mask  
    
def wrap_text(text, width):
    """Wrap text to a specified width."""
    return '\n'.join(textwrap.wrap(text, width=width))   

def save_figure_to_temp_pdf(fig, temp_pdf):
    """Save the figure to a temporary PDF file."""
    with PdfPages(temp_pdf) as pdf:
        pdf.savefig(fig)
    plt.close(fig)
    
def append_to_first_pdf(pdf1_path, pdf2_path):
    # Open the first PDF file in read mode
    with open(pdf1_path, 'rb') as pdf1_file:
        # Create a PDF reader object for the first PDF
        pdf1_reader = PyPDF2.PdfReader(pdf1_file)
        
        # Open the second PDF file in read mode
        with open(pdf2_path, 'rb') as pdf2_file:
            # Create a PDF reader object for the second PDF
            pdf2_reader = PyPDF2.PdfReader(pdf2_file)
            
            # Create a PDF writer object
            pdf_writer = PyPDF2.PdfWriter()
            
            # Add all pages from the first PDF to the writer
            for page_num in range(len(pdf1_reader.pages)):
                page = pdf1_reader.pages[page_num]
                pdf_writer.add_page(page)
            
            # Add all pages from the second PDF to the writer
            for page_num in range(len(pdf2_reader.pages)):
                page = pdf2_reader.pages[page_num]
                pdf_writer.add_page(page)
            
            # Write the combined pages to the first PDF file
            with open(pdf1_path, 'wb') as output_pdf:
                pdf_writer.write(output_pdf)   
                
def compare_dicom_image(file_path1, file_path2, output_pdf):
#    global output_pdf  # Declare output_pdf as global to use the global variable
    image1 = load_dicom_image(file_path1)
    image2 = load_dicom_image(file_path2)
    if image1 is None or image2 is None:
        print("Skipping comparison due to invalid DICOM images.")
        return    
    mse, ssim_index, diff = compare_images(image1, image2)
    # If images are similar, skip processing and exit the function
    if mse <= 1 and ssim_index >= 0.7:
#        print(f"\nComparing {file_path1} and {file_path2}:")   
#        print(f"MSE: {mse}, SSIM: {ssim_index}")
#        print(f"Images {file_path1} and {file_path2} are similar. Processing next pair...")
        return

    changed_area, mask = find_changed_area(image1, image2)
    
    print(f"\n$$$Comparing {file_path1} and {file_path2}:")
    print(f"MSE: {mse}, SSIM: {ssim_index}")

    # Create a figure with the original images and the difference image with marked changes
    fig, ax = plt.subplots(1, 3, figsize=(15, 5))
    ax[0].imshow(image1, cmap='gray')
    ax[0].set_title('Image 1')
    ax[1].imshow(image2, cmap='gray')
    ax[1].set_title('Image 2')
    ax[2].imshow(mask, cmap='gray')
    ax[2].set_title('Changed Areas')

    # Wrap the text to fit the page width
    title_text = f"Comparison: {file_path1} vs {file_path2}"
    wrapped_title = wrap_text(title_text, width=120)
    metrics_text = f"MSE: {mse:.2f}, SSIM: {ssim_index:.2f}"

    # Add wrapped title and metrics text to the figure
    plt.subplots_adjust(top=0.7)  # Adjust the top space to fit the title
    plt.suptitle(f"{wrapped_title}\n{metrics_text}\n", fontsize=12)

    # Save the figure to the temporary PDF
    temp_pdf = 'temp_output.pdf'
    save_figure_to_temp_pdf(fig, temp_pdf)

    # Check if the output PDF does not exist or is empty, copy the temporary PDF to the output PDF
    if not os.path.exists(output_pdf) or os.path.getsize(output_pdf) == 0:
        shutil.copy(temp_pdf, output_pdf)
        os.remove(temp_pdf)
        return
    else:
        append_to_first_pdf(output_pdf, temp_pdf)
        os.remove(temp_pdf)
                
def build_dicom_map(folder_path):
    dicom_map = {}
    for root, dirs, files in os.walk(folder_path):
        for file in files:
            if file.endswith('.dcm'):
                file_path = os.path.join(root, file)
                ds = pydicom.dcmread(file_path)
                sop_instance_uid = ds.SOPInstanceUID
                dicom_map[sop_instance_uid] = file_path
    return dicom_map

def read_id_map(csv_file):
    id_map = {}
    with open(csv_file, 'r') as file:
        reader = csv.DictReader(file)
        for row in reader:
            id_map[row['id_new']] = row['id_old']
    return id_map

def compare_two_batch_DICOMs(new_map, old_map, id_map, logger, output_pdf):
    for sop_instance_uid in new_map:
        id_new = sop_instance_uid
        new_file_path = new_map.get(id_new)
#        print("\n\n!!!Process new file for SOP: ", id_new, " at file path: ", new_file_path)
        logger.debug("\n\n!!!Process new file for SOP: " + id_new + " at file path: " + new_file_path)
        debug_print("\n\n!!!Process new file for SOP: "+ id_new + " at file path: "+ new_file_path)        
        id_old = id_map.get(id_new)
        if id_old:
            old_file_path = old_map.get(id_old)
            if old_file_path:
                logger.debug("comparing with file for ID_OLD " + id_old + ": " +old_file_path)
                debug_print("comparing with file for ID_OLD " + id_old + ": " +old_file_path)
#                print(f"comparing with file for ID_OLD {id_old}: {old_file_path}")     
                compare_dicom_image(new_file_path, old_file_path, output_pdf)
                
def debug_print(message):
    if DEBUG_MODE:
        print(message)
        
def main(config_file):
    # Read configuration from config.json
    with open(config_file) as config_file:
        config = json.load(config_file)        

# Paths to folders and files
    new_folder_path = config['pre_processed_file_path']
    old_folder_path = config['post_processed_file_path']
    id_map_csv_file = config['id_map_csv_file']
    output_pdf = config['output_pdf_path'];

# Ensure the directory exists
    log_directory = config['log_directory']
    os.makedirs(log_directory, exist_ok=True)

# Configure logging to write to a specific location
    logging.basicConfig(
        filename=os.path.join(log_directory, 'imgCmpLog.log'),
        level=logging.DEBUG,
        format='%(asctime)s - %(levelname)s - %(message)s'
    )

    logger = logging.getLogger()

# Start the timer
    start_time = time.time()  

# Step 1: Build new_map
    new_map = build_dicom_map(new_folder_path)

# Step 2: Build old_map
    old_map = build_dicom_map(old_folder_path)

# Step 3: Read id_map from CSV
    id_map = read_id_map(id_map_csv_file)

# Step 4 and 5: Compare two batch of DICOM files
    compare_two_batch_DICOMs(new_map, old_map, id_map, logger,output_pdf)

# Stop the timer
    end_time = time.time()

# Calculate and display the execution time
    execution_time = end_time - start_time
    print(f"Execution time: {execution_time:.2f} seconds")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Comparing DICOM files based on a configuration file")
    parser.add_argument('config', help="Path to the configuration JSON file")

    args = parser.parse_args()
    main(args.config)
