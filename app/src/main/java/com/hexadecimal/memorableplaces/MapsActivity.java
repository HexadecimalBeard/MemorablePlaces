package com.hexadecimal.memorableplaces;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    LocationManager locationManager;
    LocationListener locationListener;
    private GoogleMap mMap;

    public void centerMapOnLocation (Location location, String title) {

        if (location != null) {
            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());   // kullanicinin bulundugu konumu aldik
            mMap.clear();       // haritadaki marker'lari temizledik
            mMap.addMarker(new MarkerOptions().position(userLocation).title(title));           // kullanicinin bulundugu konuma marker ve marker ustunde cikacak basligi ekledik
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 12));           // harita acildiginda kullanicinin konumunu ne kadar yakınlastirarak gostermesi gerektigini yazdik
                                                                                                // burada ikinci integer degeri zoom miktarini belirler, 1 en az 20 en çok
        }
    }

    // istedigimiz iznin cevabini almak icin yazdik
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // herhangi bir izne onay alindi mi diye baktik
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            // konum bilgisine erismek icin izin aldik mi diye baktik
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, locationListener);

                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centerMapOnLocation(lastKnownLocation,"Your location");
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapLongClickListener(this);

        // onceki aktiviteden gelen ekstra bilgiyi almak icin yazdik
        Intent intent = getIntent();

        if(intent.getIntExtra("place number", 0) == 0){     // kullanici listeden ilk itemi sectiyse, ilk item > add a new place
            // zoom in on user location
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    centerMapOnLocation(location,"Your location");
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            };

            // eger izin varsa konum bilgisini al
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, locationListener);

                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centerMapOnLocation(lastKnownLocation,"Your location");

            } else {
                // eger izin yoksa, once izni al
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            }
        } else {        // eger kullanici kaydettigi bir adresi secerse
            Location placeLocation = new Location(LocationManager.GPS_PROVIDER);
            // listemizdeki secilen adresin latitude ve longtitude bilgilerini aldik
            placeLocation.setLatitude(MainActivity.locations.get(intent.getIntExtra("place number",0)).latitude);
            placeLocation.setLongitude(MainActivity.locations.get(intent.getIntExtra("place number",0)).longitude);
            // listeden aldigimiz konuma gidip marker yerlestirmek icin centerMapOnLocation() metodumuzu cagirdik
            centerMapOnLocation(placeLocation,MainActivity.places.get(intent.getIntExtra("place number",0)));
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        // kullanicinin su anda bulundugu konumu adres olarak almak icin yazdik
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        String address = "";

        try{
            // geocoder'in icindeki bilgileri adres tipinde veri saklayan bir listeye atadik
            List<Address> listAddresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            if(listAddresses != null && listAddresses.size() > 0) {
                if (listAddresses.get(0).getThoroughfare() != null){
                    if(listAddresses.get(0).getSubThoroughfare() != null ) {
                        address += listAddresses.get(0).getSubThoroughfare() + " ";
                    }
                    address += listAddresses.get(0).getThoroughfare();
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        // eger marker yerlestirdigimiz yerden adres bilgisi alamiyorsak marker yerlestirdigimiz saati baslik olarak atiyoruz
        if(address.equals("")){
            // kullanicinin anlik zamanini alan kisim
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm yyyy-MM-dd");
            // buradaki new Date() metodu bizim icin kullanilan cihazin zamanini alir, verdigimiz formatta string'e cevirir
            address += sdf.format(new Date());
        }

        mMap.addMarker(new MarkerOptions().position(latLng).title(address));

        // adres ve su andaki konum bilgisini diger siniftaki array'lere yerlestirdik
        MainActivity.places.add(address);
        MainActivity.locations.add(latLng);
        // gerekli bilgileri array'lere yerlestirdikten sonra listview'imizi guncelledik
        MainActivity.arrayAdapter.notifyDataSetChanged();

        Toast.makeText(this, "Location Saved!", Toast.LENGTH_SHORT).show();
    }
}
