package com.example.wante.dotjariapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, View.OnClickListener {

    private static final int REQUEST_CODE_PERMISSIONS = 1000;
    private GoogleMap mMap;

    //주소검색 api
    private String _key = "\t6c167f32479c07fbb1547685355524";

    private ListView _addressListView;
    private ArrayAdapter<String> _addressListAdapter;
    private String _putAddress; // 사용자가 입력한 주소
    private ArrayList<String> _addressSearchResultArr = new ArrayList<String>();  //우체국으로부터 반환 받은 우편주소 리스트



    //지오코드
    private Geocoder geocoder;
    private Button addressButton, _addressListBtn;
    private EditText addressEditText;

    private FusedLocationProviderClient mFusedLocationClient;


    //fab
    private Animation fab_open, fab_close;
    private Boolean isFabOpen = false;
    private FloatingActionButton fab, fab1, fab2;
    //fab

    //
    private String getAddress = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        addressButton = (Button) findViewById(R.id.address_btn);
        addressEditText = (EditText) findViewById(R.id.et_searchAddress);
        _addressListView = (ListView) findViewById(R.id.lv_address_list);
        //주소검색에 글자가 입력될 때
/*

        _addressListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAddress (addressEditText.getText().toString());
            }
        });
*/



        //FAB
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);

        fab = (FloatingActionButton) findViewById(R.id.fab_open);
        fab1 = (FloatingActionButton) findViewById(R.id.fab_nowLocation);
        fab2 = (FloatingActionButton) findViewById(R.id.fab_markerRemove);


        fab.setOnClickListener(this);
        fab1.setOnClickListener(this);
        fab2.setOnClickListener(this);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //fab
                if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MapsActivity.this,
                            new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                            REQUEST_CODE_PERMISSIONS);
                    return;
                }
                mFusedLocationClient.getLastLocation().addOnSuccessListener(MapsActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if( location != null) {
                            LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());


                            // 위도경도 지오코드 변경
                            Geocoder geocoder = new Geocoder(MapsActivity.this,Locale.KOREA);
                            List<Address> address; 
                            String nowAddress = "위치확인 실패";
                            
                            try {
                                if(geocoder != null) {
                                    address = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                                if(address != null && address.size() > 0) {
                                    String currentLocationAddress = address.get(0).getAddressLine(0).toString();
                                    nowAddress = currentLocationAddress;
                                }
                                
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                            mMap.addMarker(new MarkerOptions()
                                    .position(myLocation)
                                    .title(nowAddress));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));

                            Toast.makeText(MapsActivity.this, "현재 위치로 이동합니다.", Toast.LENGTH_SHORT).show();

                            getAddress = nowAddress;
                        }
                    }
                });

            }
        },500);


    }
/*// 주소검색 api
    private void getAddress(String kAddress) {

        _putAddress = kAddress;
        new GetAddressDataTask().execute();

    }*/


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;
        geocoder = new Geocoder(this);



/*        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(37.52487, 126.927237);
        mMap.addMarker(new MarkerOptions().position(sydney).title(" "));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/

        // 카메라 당기기
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));


        //맵터치이벤트
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {




                final MarkerOptions mOptions = new MarkerOptions();
              mOptions.title("마커주소");
              Double latitude = latLng.latitude; // 위도
                Double longitude = latLng.longitude; // 경도


                Geocoder geocoder = new Geocoder(MapsActivity.this,Locale.KOREA);
                List<Address> address;
                String markerAddress = "위치확인 실패";

                try {
                    if(geocoder != null) {
                        address = geocoder.getFromLocation(latitude,longitude,1);
                        if(address != null && address.size() > 0) {
                            String currentLocationAddress = address.get(0).getAddressLine(0).toString();
                            markerAddress = currentLocationAddress;
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }



                // 마커의 스니펫 설정
                mOptions.snippet(markerAddress);



                //LatLng 위도경도 쌍을 나타냄
                mOptions.position(new LatLng(latitude, longitude));
                //마커핀추가
                googleMap.addMarker(mOptions);

                getAddress = markerAddress;

            }
        });


        // 주소찾기 버튼 이벤트
        addressButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                String str = addressEditText.getText().toString();
                List<Address> addressList = null;

                try {
                    //editText 에 입력한 텍스트를 지오코딩을 이용해 변환한다.

                    addressList = geocoder.getFromLocationName(str,10);  // 주소와 최대검색결과개수
                } catch (IOException e) {
                    e.printStackTrace();
                }

                System.out.println(addressList.get(0).toString());
                String [] splitStr = addressList.get(0).toString().split(",");
                String address = splitStr[0].substring(splitStr[0].indexOf("\"") +1, splitStr[0].length()-2); //주소
                System.out.println(address);
                getAddress = address;
                String latitude = splitStr[10].substring(splitStr[10].indexOf("=") + 1); //위도
                String longitude = splitStr[12].substring(splitStr[12].indexOf("=") +1); //경도

                System.out.println(latitude);
                System.out.println(longitude);

                // 좌표(위도,경도) 생성
                LatLng point = new LatLng(Double.parseDouble(latitude),Double.parseDouble(longitude));

                // 마커 생성
                MarkerOptions mOptions2 = new MarkerOptions();
                mOptions2.title("검색주소");
                mOptions2.snippet(address);
                mOptions2.position(point);
                //마커추가
                mMap.addMarker(mOptions2);
                //해당 좌표로 화면 줌
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point,15));
            }
        });


        //클릭이벤트
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Toast.makeText(MapsActivity.this, "화면클릭", Toast.LENGTH_SHORT).show();
            }
        });


    }


        public static String getAddress(Context mContext, double lat, double lng) {
            String nowAddress = "현재 위치를 확인 할 수 없습니다.";
            Geocoder geocoder = new Geocoder(mContext, Locale.KOREA);
            List<Address> addressList;

            try{
                if(geocoder != null) {
                    //세번째 파라미터는 좌표에 대해 주소를 리턴받는 갯수
                    // 한좌표에 대해 두개이상의 이름이 존재할수 있기에 주소배열을 리턴받기 위해 최대갯수 설정
                    addressList = geocoder.getFromLocation(lat,lng,1);

                    if( addressList != null && addressList.size() > 0) {
                        //주소 받아오기
                        String currentLocationAddress = addressList.get(0).getAddressLine(0).toString();
                        nowAddress = currentLocationAddress;
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return nowAddress;
        }



    // 권한이 거부될경우
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_CODE_PERMISSIONS:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "권한 거부 됨", Toast.LENGTH_SHORT).show();
                }
        }

    }


    // 마커클릭
    @Override
    public boolean onMarkerClick(Marker marker) {
        Toast.makeText(this, "마커클릭", Toast.LENGTH_SHORT).show();

        return false;
    }






    // 검색완료
    public void onConfirmButtonClicked(View view) {

        // 쓰기창으로 이동하고 주소정보를 보냄
        Intent confirmIntent = new Intent(MapsActivity.this, ShopRegisterActivity.class);
        confirmIntent.putExtra("putAddress", getAddress);
        startActivity(confirmIntent);


    }


    // fab
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.fab_open:
                anim();
                break;
            case R.id.fab_nowLocation:   // 현재위치이동버튼
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                            REQUEST_CODE_PERMISSIONS);
                    return;
                }
                mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if( location != null) {
                            LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.addMarker(new MarkerOptions()
                                    .position(myLocation)
                                    .title("현재위치"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));

                            Toast.makeText(MapsActivity.this, "현재 위치로 이동합니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                anim();
                break;
            case R.id.fab_markerRemove: // 마커로 위치이동버튼
                mMap.clear();
                anim();
                break;

        }
        //fab

    }

    public void anim() {

        if (isFabOpen) {
            fab1.startAnimation(fab_close);
            fab2.startAnimation(fab_close);

            fab1.setClickable(false);
            fab2.setClickable(false);

            isFabOpen = false;
        } else {
            fab1.startAnimation(fab_open);
            fab2.startAnimation(fab_open);

            fab1.setClickable(true);
            fab2.setClickable(true);

            isFabOpen = true;
        }
    }

    /*// 주소검색api
    private class GetAddressDataTask extends AsyncTask<String, Void, HttpResponseCache> {

        @Override
        protected HttpResponseCache doInBackground(String... urls) {

            HttpResponseCache responseCache = null;
            final String apiurl = "https://biz.epost.go.kr/KpostPortal/openapi";

            ArrayList<String> addressInfo = new ArrayList<String>();

            HttpURLConnection conn = null;

            try {
                StringBuffer sb = new StringBuffer(3);
                sb.append(apiurl);
                sb.append("?regkey="+ _key +"&target=post&query=");
                sb.append(URLEncoder.encode(_putAddress,"EUC-KR"));
                String query = sb.toString();

                URL url = new URL(query);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("accept-language","ko");

                DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                byte[] bytes = new byte[4096];
                InputStream in = conn.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                while (true){
                    int red = in.read(bytes);
                    if(red<0)
                        break;
                    baos.write(bytes, 0, red);
                }
                String xmlData = baos.toString("utf-8");
                baos.close();
                in.close();
                conn.disconnect();

                Document document = documentBuilder.parse(new InputSource(new StringReader(xmlData)));
                Element el = (Element) document.getElementsByTagName("itemlist").item(0);

                for (int i = 0; i < ((Node)el).getChildNodes().getLength(); i++){
                    Node node = ((Node) el).getChildNodes().item(i);
                    if (!node.getNodeName().equals("item")){
                        continue;
                    }
                    String address = node.getChildNodes().item(1).getFirstChild().getNodeValue();
                    String post = node.getChildNodes().item(3).getFirstChild().getNodeValue();
                    Log.w("jaeha","address = " + address);
                    addressInfo.add(address + "\n우편번호:" + post.substring(0, 3) +"-"+ post.substring(3));
                }
                    _addressSearchResultArr = addressInfo;
                publishProgress();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try{
                        if(conn != null)
                            conn.disconnect();
                }catch (Exception e){

                }
            }
            return responseCache;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

            String[] addressStrArray = new String[_addressSearchResultArr.size()];
            addressStrArray = _addressSearchResultArr.toArray(addressStrArray);

            _addressListAdapter = new ArrayAdapter<String>(MapsActivity.this, android.R.layout.simple_list_item_1, addressStrArray);
            _addressListView.setAdapter(_addressListAdapter);
        }
    }

*/


}
