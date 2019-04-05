#This file is referenced from https://docs.microsoft.com/en-us/bingmaps/articles/bing-maps-tile-system #provided in the assignment document. 
import math
import cv2
import os

MinLat = -85.05112878
MaxLat = 85.05112878
Highest_Level = 23
MaxLong = 180
EarthRadius = 6378137
MinLong = -180

def mapSize(level):
	#Returns the map width and height.
	return 256 << level

def clip(n, minval, maxval):
	#Clip a number to the specified min and max values.	
	Maxi = max(n, minval)
	Mini = min(Maxi, maxval)
	return Mini
	
def mapScale(latitude, level, dpi):
	#Returns the map scale at a specified latitude, level of detail, and screen resolution.
	g = groundResolution(latitude, level)
	result = g * dpi/0.0254
	return result
	
def groundResolution(latitude, level):
	#Returns ground resolution in meters per pixel.
	latitude = clip(latitude, MinLat, MaxLat)
	cosi = math.cos(latitude*math.pi / 180)
	r = cosi * 2 * math.pi * EarthRadius
	s = mapSize(level)
	result = r / s
	return result 


def latLongToPixelXY(latitude, longitude, level):
	#Convert a point from latitude/longitutde into pixel XY coordinates at a specified level of detail.
	longitude = clip(longitude, MinLong, MaxLong)
	x1 = (longitude + 180)
	x = x1 / 360
	latitude = clip(latitude, MinLat, MaxLat)
	mapsize = mapSize(level)
	sinOfLat = math.sin(latitude * math.pi / 180)
	pixelX = int(clip(x * mapsize + 0.5, 0, mapsize - 1))
	y1 = math.log((1 + sinOfLat) / (1 - sinOfLat))
	y2 =  (4 * math.pi)
	y = 0.5 -  y1 / y2
	pixelY = int(clip(y * mapsize + 0.5, 0, mapsize - 1))

	return pixelX, pixelY

def pixelXYToTileXY(pixelX, pixelY):
	return int(pixelX / 256),int(pixelY / 256)

def tileXYToQuadKey(tileX, tileY, level):
	#Returns a QuadKey for the given tileX/tileY/level.
	#referenced from: https://docs.microsoft.com/en-us/bingmaps/articles/bing-maps-tile-system
	quadkey = ""
	for i in range(level, 0, -1):
		digit = '0'
		mask = 1 << (i-1)
		if ((tileY & mask) != 0):
			digit = chr(ord(digit) + 1)
			digit = chr(ord(digit) + 1)
		if ((tileX & mask) != 0):
			digit = chr(ord(digit) + 1)
		quadkey += digit
	return quadkey



def latLongToTileXY(latitude, longitude, level):
	#Convert a point from latitude/longitutde into tile XY coordinates at a specified level of detail.
	pixelX, pixelY = latLongToPixelXY(latitude, longitude, level)
	tileX, tileY = pixelXYToTileXY(pixelX, pixelY)
	return tileX, tileY



