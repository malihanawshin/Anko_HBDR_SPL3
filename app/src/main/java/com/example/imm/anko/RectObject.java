package com.example.imm.anko;

import android.graphics.Bitmap;

    public class RectObject implements Comparable<RectObject> {
        int xCord;
        Bitmap bmp;

        public RectObject(int xCord, Bitmap bmp) {
            this.xCord = xCord;
            this.bmp = bmp;
        }

        public int compareTo(RectObject roi) {
            if(this.xCord < roi.xCord) {
                return -1;
            }
            else if(this.xCord > roi.xCord) {
                return 1;
            }
            else {
                return 0;
            }
        }
    }

