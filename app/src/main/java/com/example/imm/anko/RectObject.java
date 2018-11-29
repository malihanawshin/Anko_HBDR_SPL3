package com.example.imm.anko;

import android.graphics.Bitmap;

    public class RectObject implements Comparable<RectObject> {
        int xCoordinate;
        Bitmap bitmap;

        public RectObject(int xCoordinate, Bitmap bitmap) {
            this.xCoordinate = xCoordinate;
            this.bitmap = bitmap;
        }

        public int compareTo(RectObject ro) {
            if(this.xCoordinate < ro.xCoordinate) {
                return -1;
            }
            else if(this.xCoordinate > ro.xCoordinate) {
                return 1;
            }
            else {
                return 0;
            }
        }
    }

