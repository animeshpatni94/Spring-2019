import numpy as np
import pptk
import tqdm
import pandas as pd
import pcl
import math


cloud_file = "final_project_point_cloud.fuse"
inlier_file = "inliers.pcd"
outlier_file = "outliers.pcd"


def read_data_file(filename, mode="fuse"):
    data = []
    intensity = []
    df = None

    if mode == "fuse":
        print("Reading Fuse file")
        df = pd.read_csv(filename, sep=" ", header=None)
    if mode == "pcd":
        print("Reading PCD file")
        df = pd.read_csv(filename, sep=" ", header=None, skiprows=11)

    pbar = tqdm.tqdm(total=len(df.index))
    for i, line in df.iterrows():
        if mode == "fuse":
            data_line = [line[0], line[1], line[2]]
            intensity.append(line[3])
            data.append(data_line)
        if mode == "pcd":
            data_line = [line[0], line[1], line[2]]
            data.append(data_line)
        pbar.update(1)
    pbar.close()
    return data, intensity

def ecef(lat, lon, alt):
    rad_lat = lat * (math.pi / 180.0)
    rad_lon = lon * (math.pi / 180.0)

    a = 6378137.0
    finv = 298.257223563
    f = 1 / finv
    e2 = 1 - (1 - f) * (1 - f)
    v = a / math.sqrt(1 - e2 * math.sin(rad_lat) * math.sin(rad_lat))

    x = (v + 0) * math.cos(rad_lat) * math.cos(rad_lon)
    y = (v + 0) * math.cos(rad_lat) * math.sin(rad_lon)
    z = alt

    return [x, y, z]

def convert_to_ecef(lines):
    data = []
    for line in lines:
        data_line = ecef(line[0], line[1], line[2])
        data.append(data_line)
    return data


def process_cloud_ponts(data):

    print("Please wait.. Processing Cloud Points..")
    point_cloud = pcl.PointCloud(data)
    fil = point_cloud.make_statistical_outlier_filter()
    fil.set_mean_k(40)
    fil.set_std_dev_mul_thresh(0.75)

    print("Storing Inlier points")
    pcl.save(fil.filter(), inlier_file)

    print("Storing Outlier points")
    fil.set_negative(True)
    pcl.save(fil.filter(), outlier_file)


def visualize_points(data, filename, mode="height", att=None):
    cloud_points = np.array(convert_to_ecef(data))
    if mode == "height":
        v = pptk.viewer(cloud_points, cloud_points[:, 2])
        v.set(point_size=0.02,show_info=True, show_grid=True, floor_color=[1,1,1,1], show_axis=False)
        v.capture(filename)
    elif mode == "intensity":
        v = pptk.viewer(cloud_points)
        v.attributes(np.array(att))
        v.set(point_size=0.02,show_info=False, show_grid=True, show_axis=False)
        v.capture(filename)


def main():

    print("Please wait.. Loading data.. ")
    data_arr, intensity = read_data_file(cloud_file, mode="fuse")
    while 1:
        print("Cloud Point Processing")
        print("1. Visualize Cloud Points by Intensity")
        print("2. Visualize Cloud Points by Height/z-axis")
        print("3. Filter Cloud Points")
        print("4. Exit")

        choice = input("Please enter your choice ")
        if choice == "1":
            visualize_points(data_arr, "cp_intensity.png", mode="intensity", att=intensity)
        elif choice == "2":
            visualize_points(data_arr, "cp_elevation.png")
        elif choice == "3":
            process_cloud_ponts(data_arr)
            inliers, intensity = read_data_file(inlier_file, mode="pcd")
            outliers, intensity = read_data_file(outlier_file, mode="pcd")
            visualize_points(inliers, "cp_inliers.png")
            visualize_points(outliers, "cp_outliers.png")
        elif choice == "4":
            print("Bye")
            break
        else:
            print("Invalid Input. System Exit.")
            break

if __name__ == '__main__':
    main()