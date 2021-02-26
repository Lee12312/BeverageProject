import cv2
import numpy as np
from PIL import ImageFont, ImageDraw, Image

fontpath = "fonts/gulim.ttc"
font = ImageFont.truetype(fontpath, 20)
#### global ####
x,y,k = 200,200,-1

cap = cv2.VideoCapture(0)

def take_inp(event, x1, y1, flag, param):
    global x, y, k
    if event == cv2.EVENT_LBUTTONDOWN:
        x = x1
        y = y1
        k = 1

cv2.namedWindow("enter_point")
cv2.setMouseCallback("enter_point", take_inp)

##### taking input point ######################
while True:
     
    _, inp_img = cap.read()
    inp_img = cv2.flip(inp_img, 1)
    gray_inp_img = cv2.cvtColor(inp_img, cv2.COLOR_BGR2GRAY)
    
    cv2.imshow("enter_point", inp_img)
    
    if k == 1 or cv2.waitKey(30) == 27:
        cv2.destroyAllWindows()
        break

##############################################
stp = 0
########## opical flow starts here ###########


old_pts = np.array([[x, y]], dtype=np.float32).reshape(-1,1,2)


mask = np.zeros_like(inp_img)

while True:
    _, new_inp_img = cap.read()
    new_inp_img = cv2.flip(new_inp_img, 1)
    new_gray = cv2.cvtColor(new_inp_img, cv2.COLOR_BGR2GRAY)     
    new_pts,status,err = cv2.calcOpticalFlowPyrLK(gray_inp_img, 
                         new_gray, 
                         old_pts, 
                         None, maxLevel=1,
                         criteria=(cv2.TERM_CRITERIA_EPS | cv2.TERM_CRITERIA_COUNT,
                                                         15, 0.08))

    for i, j in zip(old_pts, new_pts):
        x,y = j.ravel()
        a,b = i.ravel()
        if cv2.waitKey(2) & 0xff == ord('q'):
            stp = 1
            
        elif cv2.waitKey(2) & 0xff == ord('w'):
            stp = 0
        
        elif cv2.waitKey(2) == ord('n'):
            mask = np.zeros_like(new_inp_img)
            
        if stp == 0:
            mask = cv2.line(mask, (a,b), (x,y), (0,0,255), 6)

        cv2.circle(new_inp_img, (x,y), 6, (0,255,0), -1)
    
    new_inp_img = cv2.addWeighted(mask, 0.3, new_inp_img, 0.7, 0)
    cv2.putText(mask, "'n' - clear", (10,50), 
                cv2.FONT_HERSHEY_PLAIN, 1.5, (255,255,255),2)
    img_k = Image.fromarray(cv2.cvtColor(mask, cv2.COLOR_BGR2RGB))
    draw = ImageDraw.Draw(img_k)
    
    draw.text((10, 70),  " 배경은 민무늬!!!, 멀리서 움직일수록, 잘 탐색. ", font=font, fill=(255,255,255,0))
    mask = cv2.cvtColor(np.array(img_k), cv2.COLOR_RGB2BGR)
    cv2.imshow("ouput", new_inp_img)
    cv2.imshow("result", mask)

    
    gray_inp_img = new_gray.copy()
    old_pts = new_pts.reshape(-1,1,2)
    
    if cv2.waitKey(1) & 0xff == 27:
        print("끗")
        break
    

cv2.destroyAllWindows()
cap.release()