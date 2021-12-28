import cv2
import PIL.Image as Image
import io
import base64
import numpy as np


def compare(source, target):
    byteSource = base64.b64decode(source)
    byteTarget = base64.b64decode(target)
    source = Image.open(io.BytesIO(byteSource))
    target = Image.open(io.BytesIO(byteTarget))
    return get_ssim(np.array(source), np.array(target))

def get_ssim(img1, img2):
    img1 = cv2.cvtColor(np.array(img1), cv2.COLOR_RGB2BGR)
    img1 = cv2.cvtColor(np.array(img1), cv2.COLOR_RGB2BGR)
    orb_similarity = orb_sim(img1, img2)
    text = "ORB Sim : " + str(int(orb_similarity*10000)/100) + "%"
    return text

def orb_sim(img1, img2):
    orb = cv2.ORB_create()
    kp_a, desc_a = orb.detectAndCompute(img1, None)
    kp_b, desc_b = orb.detectAndCompute(img2, None)
    bf = cv2.BFMatcher(cv2.NORM_HAMMING, crossCheck=True)
    matches = bf.match(desc_a, desc_b)
    similar_regions = [i for i in matches if i.distance < 50]
    if len(matches) == 0:
        return 0
    return len(similar_regions) / len(matches)
