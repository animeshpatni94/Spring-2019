
# Satellite/Aerial Image Retrieval


## Objective: 
Download the aerial image with the possible best quality(finest resolution) of the given bouding box latitude and longitude provided by the user.


***Input:*** lat1, lon1, lat2, lon2

***Output:*** an aerial imagery within the bounding box defined with the best possible resolution


## Execution Instructions


run the terminal in the project directory

    # Example for Los Angeles
    python main.py 34.105290 -118.656758 33.570656 -117.58082
	
------------------------------------or-----------------------------------------

    # Example for Manhattan
    $ python main.py 40.811149 -74.031106 40.69188 -73.929692

The output image is then saved as 'result.jpg' in the project directory, 


## Required Environment

**Python 3.6 or above**

**Pillow (PIL Fork) 5.1.x**

Note:

1. Installation of PIL:  

		$ pip install Pillow
	
2. Make sure the 'null.jpeg' file is present in the project directory.



## Project files
	main.py

	map_tile.py

	null.jpeg


## Algorithm 

1. Calculating the lowest possible acceptable level by all bounding box area within one tile.

2. Calculating the best possible level by filtering out from fine to coarse iteratively.

3. Query each tile image and stich.

      1) Convert latitude,longitude to pixel coordinates.
	
      2) Convert pixel coordinates to tile coordinates.
	
      3) Query the tile image.
	
4. Refine and crop the generated image by each pixel.



## Reference

***http://msdn.microsoft.com/en-us/library/bb259689.asp***



