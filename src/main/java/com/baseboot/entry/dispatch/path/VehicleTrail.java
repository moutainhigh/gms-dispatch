package com.baseboot.entry.dispatch.path;

public class VehicleTrail extends Path{

    public static VehicleTrail createVehicleTrail(byte[] bytes) {
        VehicleTrail vehicleTrail = new VehicleTrail();
        vehicleTrail.parseBytes2Path(bytes);
        return vehicleTrail;
    }

}
