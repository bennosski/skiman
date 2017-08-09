package com.example.ben.skiman3;

import java.util.ArrayList;

/**
 * Created by Ben on 6/21/2017.
 */

//
// determines the positions and time intervals of the skier during his run
// axes go from 0 to 1 and -1 to 0
//

public class Skier {
    public int N;
    public double E;
    public boolean inair;
    public double[] xs_user, ys_user;
    public String debug = "";

    public Skier(double[] xs, double[] ys, int n){

        N = n;
        xs_user = new double[N];
        ys_user = new double[N];
        for(int i=0; i<N; i++) {
            // user input
            xs_user[i] = xs[i];
            ys_user[i] = ys[i];
        }
        xs_user[0] = 0.;
        ys_user[0] = 0.;


        /*
        //optimal solution
        N = n;
        xs_user = new double[N];
        ys_user = new double[N];
        double m = 4.*Math.atan(1.0);
        double[] theta = linspace(0, m, N);
        double k = Math.sqrt(2./(m - Math.sin(m)));
        for(int i=0; i<N; i++) {
            xs_user[i] =  1. / 2 * k*k * (theta[i] - Math.sin(theta[i]));
            ys_user[i] = -1. / 2 * k*k * (1. - Math.cos(theta[i]));
        }
        */


        //jump
        /*
        xs_user = linspace(0,1,N);
        for(int i=0; i<N; i++) {
            if(i<225) ys_user[i] = 2*(xs_user[i]-0.3)*(xs_user[i]-0.3) - 2.*0.3*0.3;
            else ys_user[i] = -1.;
        }
        ys_user[0] = 0.;
        xs_user[0] = 0.;
        //*/

        /*
        //jump and oscillate
        xs_user = linspace(0,1,N);
        for(int i=0; i<N; i++) {
            if(i<225) ys_user[i] = 2 *(xs_user[i]-0.3)*(xs_user[i]-0.3) - 2 * 0.3*0.3 - 0.2;
            else ys_user[i] = xs_user[i]*xs_user[i] - 1.;
        }
        */
    }

    //give the value of y at which to compute the velocity
    //public double compute_v(double y){
    //    return Math.sqrt(2.*(E-y));
    //}

    public double[] get_params(int i, int d){
        double[] out = new double[6];

        if(i==0 && d==-1){
            out[0] = xs_user[i];
            out[1] = 0.0;
            out[2] = ys_user[i];
            out[3] = 0.0;
            out[4] = 100000.;
            out[5] = out[2] - out[4]*out[0];
        }
        else{
            out[0] = xs_user[i];
            out[1] = xs_user[i+d];
            out[2] = ys_user[i];
            out[3] = ys_user[i+d];
            out[4] = (out[3]-out[2])/(out[1]-out[0]);
            out[5] = out[2] - out[4]*out[0];

        }
        return out;
    }

    public double[] compute_v_on_path(int i, int d){
        double[] p = get_params(i,d);
        double x0,x1,y0,y1,a,b;
        x0=p[0]; x1=p[1]; y0=p[2]; y1=p[3]; a=p[4]; b=p[5];
        double vx,vy;
        vx = d * Math.sqrt(2.*(E-y1)) / Math.sqrt(1. + a*a);
        vy = vx * a;
        double[] out = new double[2];
        out[0] = vx;
        out[1] = vy;
        return out;
    }

    public double compute_dt(double a, double yf, double yi, double xf, double xi, double turn_factor){
        double f=0;
        if(Math.abs(yf-yi)>Math.abs(xf-xi)) {
            f = (yf-yi) / a;
        }
        else{
            f = xf-xi;
        }
        double eps=0.;
        double den = Math.sqrt(E-yf)+Math.sqrt(E-yi);
        if(den<0.00000000000001) eps=0.0000000001;
        return Math.abs(turn_factor * Math.sqrt(2.)*Math.sqrt(1+a*a)*f/(den+eps));
    }

    public class skierDataStruct{
        public double[] xs_skier, ys_skier, dts_skier;
        public boolean mybool;

        public void clear(){
            xs_skier = null;
            ys_skier = null;
            dts_skier = null;
            mybool = false;
        }
    }

    public double[] linspace(double start, double end, int num){
        double delta = (end-start)/(num-1);

        double[] out = new double[num];
        for(int i=0; i<num; i++){
            out[i] = start + delta*i;
        }
        return out;
    }

    public double mysum(ArrayList<Double> arr){
        double sum = 0.;
        for(int i=0; i<arr.size(); i++)
            sum += arr.get(i);
        return sum;
    }
    //x,y,dt for ball moving on path from interval x_i to x_i+d or in the air
    //d is direction {+1,-1}
     public double x,y;
     public ArrayList<String> messages = new ArrayList<String>();
     public int where = 0;
     public void next_time_step(skierDataStruct returnData){
         //try {
             double eps = -1e-1;
             double yf, xf, turn_factor, dt, a1;
             double vx, vy;
             double[] params = get_params(myi, myd);

             if (!inair) {
                 double x0 = params[0];
                 double x1 = params[1];
                 double y0 = params[2];
                 double y1 = params[3];
                 double a = params[4];
                 double b = params[5];

                 //if turnaround point then yf = loc at turnaround
                 //and set final loc = starting point
                 //and d=-d
                 if ((E<=Math.max(y0, y1)) && !(myi==0 && myd==1)) {
                     where = 1;
                     //debug = "e1 i"+Integer.toString(myi)+Integer.toString(myd);
                     //returnData.mybool = true;
                     //int test = 1/0;
                     //if(1==1) return;

                     x = x0;
                     y = y0;
                     myd *= -1;
                     //i=i;
                     yf = E;
                     if (Math.abs(y0 - y1) < 1e-10) {
                         xf = (x1 + x0) / 2.;
                     } else {
                         xf = x0 * (yf - y1) / (y0 - y1) + x1 * (yf - y0) / (y1 - y0);
                     }
                     turn_factor = 2.;
                     dt = compute_dt(a, yf, y0, xf, x0, 2.0);
                     params = get_params(myi, myd);
                     a1 = params[4];
                 } else {
                     where = 2;
                     myi = myi + myd;
                     xf = x1;
                     yf = y1;
                     x = x1;
                     y = y1;
                     turn_factor = 1.;
                     dt = compute_dt(a, yf, y0, xf, x0, 1.0);

                     //debug+="dt "+String.format("%1.2f", dt);
                     //returnData.mybool = true;
                     //if(1==1) return;

                     params = get_params(myi, myd);
                     a1 = params[4];
                 }

                 //check for jump
                 if (myi < N - 1) {
                     //if (myd * (a1 - a) / Math.abs(a1 * a + 1e-10) < eps) {
                     if(jumpCondition(a1,a)){
                         //debug = "e3 i"+Integer.toString(i);
                         //returnData.mybool = true;
                         //if(1==1) return;
                         inair = true;
                     }
                 }

                 if (Math.abs(dt) > 10.0) {
                     debug = "e2 i"+Integer.toString(myi);
                     returnData.mybool = true;
                     if(1==1) return;

                     myi = 0;
                     myd = 1;
                     returnData.xs_skier = null;
                     returnData.ys_skier = null;
                     returnData.dts_skier = null;
                     returnData.mybool = true;
                     return;
                 }

                 returnData.xs_skier = new double[]{x};
                 returnData.ys_skier = new double[]{y};
                 returnData.dts_skier = new double[]{dt};
                 returnData.mybool = false;
                 return;
             }

             if (inair) {
                 //debug += "jump i"+Integer.toString(i)+" ";
                 //returnData.mybool = true;
                 //if(1==1) return;

                 if (myi == 0) {
                     where = 3;
                     vx = 0.;
                     vy = 0.;


                     double freeFallTime = Math.sqrt(2. * (y - ys_user[0]));
                     int NFreeFall = 1 + (int) (Math.round(freeFallTime * 60.0));
                     returnData.xs_skier = new double[NFreeFall];
                     returnData.ys_skier = linspace(y, ys_user[0], NFreeFall);
                     returnData.dts_skier = new double[NFreeFall];
                     for (int iy = 1; iy < NFreeFall; iy++) {
                         dt = (returnData.ys_skier[iy - 1] - returnData.ys_skier[iy]) * 2. /
                                 (Math.sqrt(2. * (E - returnData.ys_skier[iy])) + Math.sqrt(2. * (E - returnData.ys_skier[iy - 1])));
                         returnData.dts_skier[iy] = Math.abs(dt);
                     }
                     double a_lin = (ys_user[1] - ys_user[0]) / (xs_user[1] - xs_user[0]);
                     double v_new = ys_user[0] * a_lin / Math.sqrt(1. + a_lin * a_lin);
                     E = 0.5 * v_new * v_new + ys_user[0];
                     inair = false;
                     //should technically check if jumps again here but for now assume not inair?
                     returnData.mybool = false;
                     x = 0;
                     y = ys_user[0];
                     return;
                 }

                 //debug = "e4 i"+Integer.toString(i);
                 //returnData.mybool = true;
                 //if(1==1) return;

                 //otherwise not on left boundary and continue
                 double x0_start = xs_user[myi];
                 double y0_start = ys_user[myi];
                 double[] out;
                 out = compute_v_on_path(myi - myd, myd);
                 vx = out[0];
                 vy = out[1];

                 double a = -0.5 / (vx * vx);

                 ArrayList<Double> xs = new ArrayList<Double>();
                 ArrayList<Double> ys = new ArrayList<Double>();
                 ArrayList<Double> dts = new ArrayList<Double>();

                 int ct = 0;
                 boolean landed = false;
                 while (!landed) {

                     /*
                     ct++;
                     if(ct==199){
                         debug+="ec "+Integer.toString(myi)+" "+Integer.toString(myd)+" ";
                         returnData.mybool = true;
                         return;
                     }
                     */

                     if (myi == N - 2) { //reached the end
                         break;
                     }

                     params = get_params(myi, myd);
                     double x0 = params[0];
                     double x1 = params[1];
                     double y0 = params[2];
                     double y1 = params[3];
                     double a_lin = params[4];
                     double b_lin = params[5];

                     double b = vy / vx + x0_start / (vx * vx) - a_lin;
                     double c = -0.5*x0_start*x0_start/(vx*vx) - vy/vx*x0_start + y0_start - b_lin;

                     double disc = b * b - 4.0 * a * c;
                     double x_landing = (-b - myd * Math.sqrt(disc)) / (2. * a);
                     double y_landing = y0 * (x_landing - x1) / (x0 - x1) + y1 * (x_landing - x0) / (x1 - x0);

                     if (x0==x0_start || x_landing<Math.min(x0,x1) || x_landing>Math.max(x0,x1)){
                         where = 4;
                         x = x1;
                         y = y0_start + vy / vx * (x1 - x0_start) - 0.5 * (x1 - x0_start) * (x1 - x0_start) / (vx * vx);
                         xs.add(x);
                         ys.add(y);
                         dts.add(Math.abs((x1 - x0) / vx));
                         myi = myi+myd;
                     } else { //go to landing and continue
                         double dt_part1 = Math.abs((x_landing - x0) / vx);
                         double vx_landing = vx;
                         double vy_landing = vy - (mysum(dts) + dt_part1);
                         double v_new = vx_landing / Math.sqrt(1. + a_lin * a_lin) + vy_landing * a_lin / Math.sqrt(1. + a_lin * a_lin);
                         double dt_part2;
                         E = 0.5 * v_new * v_new + y_landing;

                         //switch direction on landing
                         if (Math.signum(v_new) != Math.signum(vx_landing)) {
                             where = 5;
                             myd *= -1;
                             xf = x0;
                             yf = y0;
                             x = x0;
                             y = y0;
                             dt_part2 = compute_dt(a, yf, y_landing, xf, x_landing, 1.0);
                             params = get_params(myi, myd);
                             a1 = params[4];
                         } else if (E <= Math.max(y_landing, y1)) {
                             where = 6;
                             //check land and then turnaround
                             //rare
                             myd *= -1;
                             yf = E;
                             xf = x0 * (yf - y1) / (y0 - y1) + x1 * (yf - y0) / (y1 - y0);
                             x = x0;
                             y = y0;
                             dt_part2 = compute_dt(a, yf, y_landing, xf, x_landing, 2.0);
                             dt_part2 = dt_part2 + compute_dt(a, y0, y_landing, x0, x_landing, 1.0);
                             params = get_params(myi, myd);
                             a1 = params[4];
                         } else { //no switch and regular continue to x1
                             where = 7;
                             yf = y1;
                             xf = x1;
                             myi = myi + myd;
                             x = x1;
                             y = y1;
                             dt_part2 = compute_dt(a, yf, y_landing, xf, x_landing, 1.0);
                             params = get_params(myi, myd);
                             a1 = params[4];
                         }

                         xs.add(x);
                         ys.add(y);
                         dts.add(Math.abs(dt_part1) + Math.abs(dt_part2));
                         landed = true;

                         inair = false;
                         //check for jump
                         if (myi < N - 1) {
                             //if (myd * (a1-a_lin) / Math.abs(a1*a_lin+1e-10) < eps) {
                             if(jumpCondition(a1,a_lin)){
                                 //debug = "e5 i"+Integer.toString(i);
                                 //returnData.mybool = true;
                                 //if(1==1) return;
                                 inair = true;
                             }
                         }
                     }
                 } //end while


                 int sz = xs.size();
                 returnData.xs_skier = new double[sz];
                 returnData.ys_skier = new double[sz];
                 returnData.dts_skier = new double[sz];
                 for (int j = 0; j < sz; j++) {
                     returnData.xs_skier[j] = xs.get(j);
                     returnData.ys_skier[j] = ys.get(j);
                     returnData.dts_skier[j] = dts.get(j);
                 }
                 return;
             }

         //}
         //catch(Exception e){debug += "error";}
     }

     public boolean jumpCondition(double a1, double a_lin){
         /*
         double eps = -1e-1;
         if(myd * (a1-a_lin) / Math.abs(a1*a_lin+1e-10) < eps)
             return true;
         return false;
         */

         if(myd*(Math.atan(a1)-Math.atan(a_lin))<-Math.atan(1.)){
             return true;
         }
         return false;

     }


     public double frameTime = 0.02;
     public int myi, myd;
     public ArrayList<Double> xs_sim, ys_sim, dts_sim;
     public ArrayList<Double> xs_frames, ys_frames, dts_frames;
     public double TotalTime;
     public void runSimulation(){
         xs_sim = new ArrayList<Double>();
         ys_sim = new ArrayList<Double>();
         dts_sim = new ArrayList<Double>();

         /*
         if(ys_user[0]<-0.001){
             inair = true;
         }
         else{
             inair = false;
         }
         */
         inair = false;

         E = 0.;
         myi = 0;
         myd = 1;
         x = 0.;
         y = 0.;
         xs_sim.add(x);
         ys_sim.add(y);
         dts_sim.add(0.);
         skierDataStruct data = new skierDataStruct();
         for(int it=0; it<1400; it++){
             if(myi==N-2)  break;

             //clean up skierDataStruct
             data.clear();
             //debug += "i "+Integer.toString(i)+" d "+Integer.toString(d);
             next_time_step(data);

             /*
             if(myd!=1){
                 debug+="!d="+Integer.toString(myd)+" ";
                 break;
             }
             */

             messages.add("w "+Integer.toString(where)+" i"+Integer.toString(myi));

             if(data.mybool) break; //stopping

             int sz = data.xs_skier.length;
             debug = " w "+Integer.toString(where)+" i "+Integer.toString(myi);
             for(int k=0; k<sz; k++){
                 xs_sim.add(data.xs_skier[k]);
                 ys_sim.add(data.ys_skier[k]);
                 dts_sim.add(data.dts_skier[k]);
             }
         }
         //debug += "i "+Integer.toString(myi)+"N "+Integer.toString(N);
         TotalTime = mysum(dts_sim);


         double[] ts_sim = new double[dts_sim.size()];
         for(int i=1; i<dts_sim.size(); i++){
             ts_sim[i] = ts_sim[i-1]+dts_sim.get(i);
         }

         xs_frames = new ArrayList<Double>();
         ys_frames = new ArrayList<Double>();

         double time = 0;
         int i = 0;
         while(time<Math.min(TotalTime, 10.0)){
             while(ts_sim[i]<time)
                 i++;


             //xs_frames.add(xs_sim.get(i)*(time-ts_sim[i+1])/(ts_sim[i]-ts_sim[i+1]-delta)
             //            + xs_sim.get(i+1)*(time-ts_sim[i])/(ts_sim[i+1]-ts_sim[i]-delta));
             //ys_frames.add(ys_sim.get(i)*(time-ts_sim[i+1])/(ts_sim[i]-ts_sim[i+1]-delta)
             //            + ys_sim.get(i+1)*(time-ts_sim[i])/(ts_sim[i+1]-ts_sim[i]-delta));


             xs_frames.add(xs_sim.get(i));
             ys_frames.add(ys_sim.get(i));

             time += frameTime;
         }


     }
}

