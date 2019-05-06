import sys
import math
import io
import operator
import numpy as np
import urllib.request
from map_tile import *
from PIL import Image
import cv2

# number of pixels per tile per edge
TILE_SIZE = 256 			
# Highest ZoomLevel
HIGHEST_ZL = 23


def getImageFromQuadkey(quadkey):
	#gets the umage from the quad key provided.
	b = True
	if(b):
		with urllib.request.urlopen(getURL(quadkey)) as imgresponse:
			image = Image.open(io.BytesIO(imgresponse.read()))
			b = False
		return image

def getURL(quadkey):
	# License_key from https://msdn.microsoft.com/en-us/library/ff428642.aspx
	# key = "AmAi44BP49RJuw-O3OCduj8l5DksZ1hPGuGABWyO2ycDX0iamrAWKgxKV3ychclO"
	return "http://h0.ortho.tiles.virtualearth.net/tiles/h%s.jpeg?g=131" % (quadkey)

def getBestLevel(latitude1, longitude1, latitude2, longitude2, MinimumLevel):
	#Estimates the level with finest resolution without missing any infromation.
	USIZE = 1 << 12			
	level = 23
	if(level > 0):
		while level >= MinimumLevel:
			print ("Level: ",level)
			tilex1, tiley1 = latLongToTileXY(latitude1, longitude1, level)
			f = True
			tilex2, tiley2 = latLongToTileXY(latitude2, longitude2, level)
			if tiley1 > tiley2:
				tiley1 = tiley2
				tiley2 = tiley1
			if tilex1 > tilex2:
				tilex1 = tilex2
				tilex2 = tilex1
		
			# filter out the "overfine" levels in case of too big images
			if_condition = (tilex2 - tilex1) * TILE_SIZE
			if if_condition > USIZE:
				level = level - 1
				continue
			bb = False
			cc = True
			# filter out the level as long as where exists even one null image
			x11 = tilex2+1
			y11 = tiley2+1
			for x in range(tilex1, x11):
				for y in range(tiley1, y11):
					newQuadKey = tileXYToQuadKey(x, y, level)
					condi = False
					current_image = getImageFromQuadkey(newQuadKey)
					condi = null_Image(current_image)
					if condi:
						print("Cannot determine the tile: (%d, %d) at the level: "% (x,y),level)
						f = False
						break
				if f == bb:
					print("Breaking out of 'getBestLevel'")
					break
			if f == cc:
				break
			level = level - 1

		if f == bb:
			print("Error: No acceptable level. Please re-select the bounding box.")
		else:
			print("Finally choosing the level: %d" % level)
			return level, tilex1, tiley1, tilex2, tiley2	
	
def getMinLevel(latitude1, longitude1, latitude2, longitude2):
	#Get the lowest acceptable level given two points coordinates of bounding box.
	b = True
	if(b):
		for i in range(HIGHEST_ZL, 0, -1):
			#latitude1, longitude1: The upper left coordinates
			tilex1, tiley1 = latLongToTileXY(latitude1, longitude1, i)
			#latitude2, longitude2: The bottom left coordinates
			tilex2, tiley2 = latLongToTileXY(latitude2, longitude2, i)
			if tiley1 > tiley2:
				tiley1 = tiley2 
				tiley2 = tiley1
			if tilex1 > tilex2:
				tilex1 = tilex2
				tilex2 = tilex1

			# The lowest acceptable level is the level where the bounding box is within only one same tile.
			if (tilex2 - tilex1 <= 1):
				if(tiley2 - tiley1 <= 1):
					print("Lowest acceptable level is: ")
			#Returns the lowest acceptable level
					print(i)
					return i
	return null
	
def null_Image(img):
	#Compare the query result with the default null image. If they match, meaning that there is no data for the location on Bing server.
	#Return: True for null img, False for non-null img.
	if(img == Image.open('null.jpeg')):
		f = True
	else:
		f = False
	return f

def main():
	#User inputs
	try:
		a1 = sys.argv[1]
		latitude1 = float(a1)
		a2 = sys.argv[2]
		longitude1 = float(a2)
		a3 = sys.argv[3]
		latitude2 = float(a3)
		a4 = sys.argv[4]
		longitude2 = float(a4)
		#Getting the lowest acceptable level
		MinimumLevel = getMinLevel(latitude1, longitude1, latitude2, longitude2) 
		#Getting the final best level
		level, tilex1, tiley1, tilex2, tiley2 = getBestLevel(latitude1, longitude1, latitude2, longitude2, MinimumLevel)
		#Generting the image.
		w = (tilex2 - tilex1 + 1)
		width = w * TILE_SIZE
		h = (tiley2 - tiley1 + 1)
		height = h * TILE_SIZE
		colorBand = 'RGB'
		image = Image.new(colorBand, (width, height))
		x11 = tilex2+1
		y11 = tiley2+1
		for x in range(tilex1, x11):
			for y in range(tiley1, y11):
				newQuadKey = tileXYToQuadKey(x, y, level) 
				current_image = getImageFromQuadkey(newQuadKey)
				xx = (x - tilex1)
				start_x = xx * TILE_SIZE 
				end_x = start_x + TILE_SIZE
				yy = (y - tiley1)
				start_y = yy * TILE_SIZE
				end_y = start_y + TILE_SIZE
				image.paste(current_image, (int(start_x), int(start_y), int(end_x), int(end_y)))
		# crop the image
		px1, py1 = latLongToPixelXY(latitude1, longitude1, level)
		base_x = tilex1 * TILE_SIZE 	
		dimension_x1 = px1 - base_x 
		base_y = tiley1 * TILE_SIZE							
		dimension_y1 = py1 - base_y
		px2, py2 = latLongToPixelXY(latitude2, longitude2, level)
		try:
			dimension_x2 = px2 - base_x
			dimension_y2 = py2 - base_y
			output = image.crop((dimension_x1, dimension_y1, dimension_x2, dimension_y2))
			print("Final Image Saved under the name: result.jpg")
			image.save("result.jpg")
		except:
			print("Something went wrong while saving the image...")
	except: 
		print("Please enter all the values correctly....")
		
	

if __name__ == '__main__':
	main()
