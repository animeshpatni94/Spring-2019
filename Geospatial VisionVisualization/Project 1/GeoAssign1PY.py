
# coding: utf-8

import cv2
import numpy as np
from random import randint
import os
import glob
import sys

def detect(samplepath,savepath):
    image = []
    lstPrg = 0
    print("Pre-processing the images in the folder...")
    progressBar = 0
    data = glob.glob(samplepath+"/*.jpg")
    total_data_len = len(data)
    print("Total images in the folder:", total_data_len)
    print("Progress in pre-processing: ")
    direct = os.listdir(samplepath)
    for i in direct:
        img = cv2.imread(os.path.join(samplepath,i))
        prog = (progressBar) * 100
        gray = cv2.cvtColor(img,cv2.COLOR_BGR2GRAY)
        progress = prog / total_data_len
        eq = cv2.equalizeHist(gray)
        if progress >= lstPrg:
            print ("Progress: "+str(progress) + "%")
            lstPrg +=10
        blur = cv2.blur(eq,(3,3))
        ret,thresh = cv2.threshold(blur,127,255,cv2.THRESH_BINARY)
        progressBar += 1
        image.append(thresh)
    
    print("Creating the mean image for:", samplepath)
    mean = mean_image(image)
    cv2.imwrite(os.path.join(savepath,'mean_image.jpg'),mean)
    print('mean_image stored at:',savepath)
    
    meandtype = np.array(np.round(mean),dtype=np.uint8)
    inter_adapt = cv2.adaptiveThreshold(meandtype,255,cv2.ADAPTIVE_THRESH_MEAN_C,cv2.THRESH_BINARY, 105, 11)
    print("Creating intermediate mask for:", samplepath)
    inter_mask = cv2.bitwise_not(inter_adapt)
    cv2.imwrite(os.path.join(savepath,'intermediate_mask.jpg'),inter_mask)
    print('Intermediate_mask stored at:',savepath)
    
    k = np.ones((10,10),np.uint8)
    print("Creating Mask for:", samplepath)
    ero = cv2.erode(inter_mask,k,iterations = 5)
    dil = cv2.dilate(ero,k,iterations = 5)
    cv2.imwrite(os.path.join(savepath,'final_mask.jpg'),dil)
    print("final_mask stored at:",savepath)
    
    read = data[randint(0,total_data_len)]
    print("Image Ramdomnly Picked For Smear:",read)
    readImage = cv2.imread(read)
    
    _, contours, _ = cv2.findContours(dil,cv2.RETR_TREE,cv2.CHAIN_APPROX_SIMPLE)
    if contours:
        result = cv2.drawContours(readImage,contours,-1,(0,255,255),2)
        cv2.imwrite(os.path.join(savepath,'smear.jpg'),result)
        print("Smear image saved at:",savepath)
        return True
    return False
    
    
def mean_image(img):
    avg = []
    length = len(img)
    sum_img = img[0] * 1/length
    for i in img:
        sum_img = cv2.add(sum_img,i * 1/length)
    return sum_img

if __name__ == "__main__":
    samplepath = input("Please enter the path of the sample data: ")
    savepath = input("Please enter path for the output images: ")
    if not samplepath:
        print ("Directory not found!")
        sys.exit()
    if(detect(samplepath,savepath)!=True):
        print("Smear not Detected for:",samplepath)
    else:
        print("Smear Detected for:",samplepath)
        

