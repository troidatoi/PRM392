package com.example.project;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CheckoutActivity extends AppCompatActivity {

	private CardView btnBack, btnConfirmOrder;
	private EditText etReceiverName, etReceiverPhone, etShippingAddress;
	private android.widget.Spinner spCity, spDistrict;
	private RadioGroup rgPaymentMethod;
	private RecyclerView rvOrderItems;
	private TextView tvSubtotal, tvShippingFee, tvTotal;
	private long estimatedShippingFee = 0;

	private CheckoutAdapter checkoutAdapter;
	private List<CheckoutRow> rows;
	private double totalAmount = 0;
	private String storeId;
	private String storeName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_checkout);

		initViews();
		prefillFromProfile();
		getStoreInfo();
		loadOrderItems();
		setupRecyclerView();
		setupCityDistrictPickers();
		calculateTotal();
		setupEstimateListeners();
		setupClickListeners();
	}

	private void prefillFromProfile() {
		try {
			com.example.project.utils.AuthManager auth = com.example.project.utils.AuthManager.getInstance(this);
			com.example.project.models.User user = auth.getCurrentUser();
			if (user == null) return;

			// Name
			String name = null;
			if (user.getProfile() != null) {
				String fn = user.getProfile().getFirstName();
				String ln = user.getProfile().getLastName();
				if (fn != null || ln != null) {
					name = ((fn==null?"":fn) + " " + (ln==null?"":ln)).trim();
				}
			}
			if ((name == null || name.isEmpty()) && user.getUsername() != null) {
				name = user.getUsername();
			}
			if (name != null && name.length() > 0 && etReceiverName.getText().toString().trim().isEmpty()) {
				etReceiverName.setText(name);
			}

			// Phone
			if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty() && etReceiverPhone.getText().toString().trim().isEmpty()) {
				etReceiverPhone.setText(user.getPhoneNumber());
			}

			// Address and City (best-effort split by last comma)
			if (user.getAddress() != null && !user.getAddress().isEmpty()) {
				String addr = user.getAddress();
				if (etShippingAddress.getText().toString().trim().isEmpty()) {
					String main = addr;
					String city = null;
					int lastComma = addr.lastIndexOf(',');
					if (lastComma > 0) {
						main = addr.substring(0, lastComma).trim();
						city = addr.substring(lastComma + 1).trim();
					}
					etShippingAddress.setText(main);
					if (city != null && spCity.getSelectedItemPosition() == 0) {
						// Try to find city in spinner
						android.widget.ArrayAdapter adapter = (android.widget.ArrayAdapter) spCity.getAdapter();
						if (adapter != null) {
							for (int i = 0; i < adapter.getCount(); i++) {
								if (city.equals(adapter.getItem(i))) {
									spCity.setSelection(i);
									break;
								}
							}
						}
					}
				}
			}
		} catch (Exception ignored) {}
	}

	private void getStoreInfo() {
		storeId = getIntent().getStringExtra("store_id");
		storeName = getIntent().getStringExtra("store_name");
		
		if (storeName != null) {
			setTitle("Thanh toán - " + storeName);
		}
	}

	private void initViews() {
		btnBack = findViewById(R.id.btnBack);
		btnConfirmOrder = findViewById(R.id.btnConfirmOrder);

		etReceiverName = findViewById(R.id.etReceiverName);
		etReceiverPhone = findViewById(R.id.etReceiverPhone);
		etShippingAddress = findViewById(R.id.etShippingAddress);
		spCity = findViewById(R.id.spCity);
		spDistrict = findViewById(R.id.spDistrict);

		rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
		rvOrderItems = findViewById(R.id.rvOrderItems);

		tvSubtotal = findViewById(R.id.tvSubtotal);
		tvShippingFee = findViewById(R.id.tvShippingFee);
		tvTotal = findViewById(R.id.tvTotal);
	}

	private void loadOrderItems() {
		// Load cart items from API for the selected store
		if (rows == null) {
			rows = new ArrayList<>();
		} else {
			rows.clear();
		}
		
		com.example.project.utils.AuthManager auth = com.example.project.utils.AuthManager.getInstance(this);
		com.example.project.network.ApiService api = com.example.project.network.RetrofitClient.getInstance().getApiService();
		com.example.project.models.User user = auth.getCurrentUser();
		
		if (user == null) {
			Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		api.getCartByUser(auth.getAuthHeader(), user.getId()).enqueue(new retrofit2.Callback<com.example.project.models.ApiResponse<Object>>() {
			@Override
			public void onResponse(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call, retrofit2.Response<com.example.project.models.ApiResponse<Object>> response) {
				if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
					Object data = response.body().getData();
					try {
						java.util.Map dataMap = (java.util.Map) data;
						java.util.List stores = (java.util.List) dataMap.get("itemsByStore");
						android.util.Log.d("CheckoutActivity", "Found " + (stores != null ? stores.size() : 0) + " stores");
						
						if (stores != null && !stores.isEmpty()) {
							int idx = 0;
							for (Object s : stores) {
								try {
									java.util.Map storeGroup = (java.util.Map) s;
									if (storeGroup == null) {
										android.util.Log.w("CheckoutActivity", "Store group is null at index " + idx);
										idx++;
										continue;
									}
									
									java.util.Map storeObj = (java.util.Map) storeGroup.get("store");
									if (storeObj == null) {
										android.util.Log.w("CheckoutActivity", "Store object is null at index " + idx);
										storeObj = new java.util.HashMap();
									}
									
									// Get store ID
									String sid = null;
									Object idCandidate = storeObj.get("_id");
									if (idCandidate != null && !("null").equals(String.valueOf(idCandidate))) {
										sid = String.valueOf(idCandidate);
									}
									if (sid == null || sid.isEmpty() || "null".equals(sid)) {
										Object altId = storeGroup.get("storeId");
										if (altId != null) sid = String.valueOf(altId);
									}
									if (sid == null || sid.isEmpty() || "null".equals(sid)) {
										sid = "store_" + idx;
									}

									// Get store name
									String sname = null;
									Object nameCandidate = storeObj.get("name");
									if (nameCandidate != null && !("null").equals(String.valueOf(nameCandidate))) {
										sname = String.valueOf(nameCandidate);
									}
									if (sname == null || sname.isEmpty() || "null".equals(sname)) {
										Object city = storeObj.get("city");
										sname = city != null ? String.valueOf(city) : "Cửa hàng";
									}

									android.util.Log.d("CheckoutActivity", "Adding store header: " + sname + " (ID: " + sid + ")");
									CheckoutRow headerRow = CheckoutRow.header(sid, sname);
									rows.add(headerRow);
									
									// Get items for this store and calculate store total
									long storeTotalValue = 0;
									java.util.List storeItems = (java.util.List) storeGroup.get("items");
									if (storeItems != null && !storeItems.isEmpty()) {
										android.util.Log.d("CheckoutActivity", "Store " + sname + " has " + storeItems.size() + " items");
										for (Object it : storeItems) {
											try {
												java.util.Map item = (java.util.Map) it;
												java.util.Map product = (java.util.Map) item.get("product");
												if (product == null) {
													android.util.Log.w("CheckoutActivity", "Product is null for an item");
													continue;
												}
												
												String name = String.valueOf(product.get("name"));
												double price = product.get("price") instanceof Number ? ((Number) product.get("price")).doubleValue() : 0;
												String priceText = formatPrice((long) price) + " VNĐ";
												int quantity = item.get("quantity") instanceof Number ? ((Number) item.get("quantity")).intValue() : 1;
												
												// Calculate total for this item and add to store total
												long itemTotal = (long)(price * quantity);
												storeTotalValue += itemTotal;
												
												CartItem ci = new CartItem(name, "", priceText, R.drawable.splash_bike_background, quantity);
												ci.setStoreId(sid);
												ci.setProductId(String.valueOf(product.get("_id")));
												ci.setUnitPrice((long) price);
												
												// Load image if available
												Object imagesObj = product.get("images");
												if (imagesObj instanceof java.util.List) {
													java.util.List images = (java.util.List) imagesObj;
													if (images != null && !images.isEmpty()) {
														Object firstImg = images.get(0);
														if (firstImg instanceof java.util.Map) {
															java.util.Map imgMap = (java.util.Map) firstImg;
															Object url = imgMap.get("url");
															if (url != null) ci.setImageUrl(String.valueOf(url));
														} else {
															ci.setImageUrl(String.valueOf(firstImg));
														}
													}
												}
												
												rows.add(CheckoutRow.item(ci));
											} catch (Exception itemEx) {
												android.util.Log.e("CheckoutActivity", "Error parsing item: " + itemEx.getMessage(), itemEx);
											}
										}
									} else {
										android.util.Log.w("CheckoutActivity", "Store " + sname + " has no items");
									}
									
									// Set store total to header row
									headerRow.setStoreTotal(storeTotalValue);
									idx++;
								} catch (Exception storeEx) {
									android.util.Log.e("CheckoutActivity", "Error parsing store at index " + idx + ": " + storeEx.getMessage(), storeEx);
									idx++;
								}
							}
							android.util.Log.d("CheckoutActivity", "Total rows added: " + rows.size());
						} else {
							android.util.Log.w("CheckoutActivity", "No stores found or empty list");
						}
					} catch (Exception e) {
						android.util.Log.e("CheckoutActivity", "Error parsing cart data: " + e.getMessage(), e);
						e.printStackTrace();
						Toast.makeText(CheckoutActivity.this, "Lỗi parse giỏ hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
					}
					
					// Update adapter with new data on UI thread
					runOnUiThread(() -> {
						if (checkoutAdapter == null) {
							checkoutAdapter = new CheckoutAdapter(rows);
							LinearLayoutManager layoutManager = new LinearLayoutManager(CheckoutActivity.this);
							rvOrderItems.setLayoutManager(layoutManager);
							rvOrderItems.setAdapter(checkoutAdapter);
						} else {
							checkoutAdapter.updateData(rows);
						}
						android.util.Log.d("CheckoutActivity", "Adapter updated, rows count: " + rows.size());
						calculateTotal();
					});
				} else {
					Toast.makeText(CheckoutActivity.this, "Lỗi tải giỏ hàng", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onFailure(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call, Throwable t) {
				Toast.makeText(CheckoutActivity.this, "Lỗi tải giỏ hàng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	private String formatPrice(long price) {
		NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
		return formatter.format(price);
	}

	private void setupRecyclerView() {
		checkoutAdapter = new CheckoutAdapter(rows);
		LinearLayoutManager layoutManager = new LinearLayoutManager(this);
		rvOrderItems.setLayoutManager(layoutManager);
		rvOrderItems.setAdapter(checkoutAdapter);
	}

	private void calculateTotal() {
		totalAmount = 0;

		for (CheckoutRow row : rows) {
			if (row.getType() == CheckoutRow.Type.ITEM && row.getItem() != null) {
				CartItem item = row.getItem();
				String priceStr = item.getPrice().replace(" VNĐ", "").replace(".", "");
				try {
					double price = Double.parseDouble(priceStr);
					totalAmount += price * item.getQuantity();
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
		}

		// Format currency
		NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
		String formattedTotal = formatter.format(totalAmount) + " VNĐ";

		tvSubtotal.setText(formattedTotal);
		updateTotalsUI();
	}

	private void updateTotalsUI() {
		NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
		String formattedSubtotal = formatter.format(totalAmount) + " VNĐ";
		String formattedShip = formatter.format(estimatedShippingFee) + " VNĐ";
		String formattedTotal = formatter.format(totalAmount + estimatedShippingFee) + " VNĐ";

		tvSubtotal.setText(formattedSubtotal);
		tvShippingFee.setText(estimatedShippingFee > 0 ? formattedShip : "Đang tính...");
		tvTotal.setText(formattedTotal);
	}

	private void setupClickListeners() {
		btnBack.setOnClickListener(v -> finish());

		btnConfirmOrder.setOnClickListener(v -> confirmOrder());
	}

	private void setupEstimateListeners() {
		android.text.TextWatcher watcher = new android.text.TextWatcher() {
			private android.os.Handler handler = new android.os.Handler();
			private Runnable work;
			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
			@Override public void afterTextChanged(android.text.Editable s) {
				if (work != null) handler.removeCallbacks(work);
				work = () -> estimateShippingIfReady();
				handler.postDelayed(work, 600);
			}
		};
		etShippingAddress.addTextChangedListener(watcher);
		// Spinner listeners are already set in setupCityDistrictPickers
		// Trigger initial estimation if fields prefilled
		estimateShippingIfReady();
	}

	private void estimateShippingIfReady() {
		String addr = etShippingAddress.getText().toString().trim();
		int cityPos = spCity.getSelectedItemPosition();
		int districtPos = spDistrict.getSelectedItemPosition();
		if (cityPos <= 0 || addr.isEmpty()) {
			estimatedShippingFee = 0;
			updateTotalsUI();
			return;
		}
		String city = spCity.getSelectedItem().toString();
		String district = (districtPos > 0 ? spDistrict.getSelectedItem().toString() : "");
		String cityForApi = (district.isEmpty() ? "" : (district + ", ")) + city;

		try {
			com.example.project.utils.AuthManager auth = com.example.project.utils.AuthManager.getInstance(this);
			com.example.project.models.User user = auth.getCurrentUser();
			if (user == null) return;

			com.example.project.network.ApiService api = com.example.project.network.RetrofitClient.getInstance().getApiService();
			java.util.Map<String, Object> body = new java.util.HashMap<>();
			body.put("userId", user.getId());
			java.util.Map<String, Object> shipping = new java.util.HashMap<>();
			shipping.put("address", addr);
			shipping.put("city", cityForApi);
			body.put("shippingAddress", shipping);

			api.estimateOrders(auth.getAuthHeader(), body).enqueue(new retrofit2.Callback<com.example.project.models.ApiResponse<Object>>() {
				@Override
				public void onResponse(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call, retrofit2.Response<com.example.project.models.ApiResponse<Object>> response) {
					if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
						try {
							Object data = response.body().getData();
							if (data instanceof java.util.Map) {
								java.util.Map d = (java.util.Map) data;
								
								// Get total shipping fee
								Object summaryObj = d.get("summary");
								if (summaryObj instanceof java.util.Map) {
									java.util.Map summary = (java.util.Map) summaryObj;
									Object totalShip = summary.get("totalShippingFee");
									if (totalShip instanceof Number) {
										estimatedShippingFee = ((Number) totalShip).longValue();
									}
								}
								
								// Update per-store headers
								Object storesObj = d.get("stores");
								if (storesObj instanceof java.util.List) {
									java.util.List stores = (java.util.List) storesObj;
									for (Object so : stores) {
										java.util.Map s = (java.util.Map) so;
										String sid = String.valueOf(s.get("storeId"));
										Double dist = null; 
										Long fee = null;
										Object dd = s.get("distanceKm"); 
										if (dd instanceof Number) dist = ((Number) dd).doubleValue();
										Object ff = s.get("shippingFee"); 
										if (ff instanceof Number) fee = ((Number) ff).longValue();
										checkoutAdapter.updateHeaderShipping(sid, dist, fee);
									}
									
									// Update bottom shipping fee display
									if (stores.size() == 1) {
										java.util.Map s = (java.util.Map) stores.get(0);
										Object distance = s.get("distanceKm");
										Object fee = s.get("shippingFee");
										long feeV = fee instanceof Number ? ((Number) fee).longValue() : estimatedShippingFee;
										NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
										tvShippingFee.setText((distance != null ? distance + " km - " : "") + fmt.format(feeV) + " VNĐ");
									} else {
										NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
										tvShippingFee.setText("Tổng phí ship: " + fmt.format(estimatedShippingFee) + " VNĐ");
									}
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					updateTotalsUI();
				}

				@Override
				public void onFailure(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call, Throwable t) {
					// Keep previous estimate; optionally show message
					updateTotalsUI();
				}
			});
		} catch (Exception ignored) {}
	}

	private void setupCityDistrictPickers() {
		// Simple static data; can be replaced by API later
		java.util.List<String> citiesList = new java.util.ArrayList<>();
		citiesList.add("-- Chọn thành phố --");
		citiesList.add("Hà Nội");
		citiesList.add("TP.HCM");
		citiesList.add("Đà Nẵng");
		citiesList.add("Hải Phòng");
		citiesList.add("Cần Thơ");
		citiesList.add("An Giang");
		citiesList.add("Bà Rịa - Vũng Tàu");
		citiesList.add("Bắc Giang");
		citiesList.add("Bắc Kạn");
		citiesList.add("Bạc Liêu");
		citiesList.add("Bắc Ninh");
		citiesList.add("Bến Tre");
		citiesList.add("Bình Định");
		citiesList.add("Bình Dương");
		citiesList.add("Bình Phước");
		citiesList.add("Bình Thuận");
		citiesList.add("Cà Mau");
		citiesList.add("Cao Bằng");
		citiesList.add("Đắk Lắk");
		citiesList.add("Đắk Nông");
		citiesList.add("Điện Biên");
		citiesList.add("Đồng Nai");
		citiesList.add("Đồng Tháp");
		citiesList.add("Gia Lai");
		citiesList.add("Hà Giang");
		citiesList.add("Hà Nam");
		citiesList.add("Hà Tĩnh");
		citiesList.add("Hải Dương");
		citiesList.add("Hậu Giang");
		citiesList.add("Hòa Bình");
		citiesList.add("Hưng Yên");
		citiesList.add("Khánh Hòa");
		citiesList.add("Kiên Giang");
		citiesList.add("Kon Tum");
		citiesList.add("Lai Châu");
		citiesList.add("Lâm Đồng");
		citiesList.add("Lạng Sơn");
		citiesList.add("Lào Cai");
		citiesList.add("Long An");
		citiesList.add("Nam Định");
		citiesList.add("Nghệ An");
		citiesList.add("Ninh Bình");
		citiesList.add("Ninh Thuận");
		citiesList.add("Phú Thọ");
		citiesList.add("Phú Yên");
		citiesList.add("Quảng Bình");
		citiesList.add("Quảng Nam");
		citiesList.add("Quảng Ngãi");
		citiesList.add("Quảng Ninh");
		citiesList.add("Quảng Trị");
		citiesList.add("Sóc Trăng");
		citiesList.add("Sơn La");
		citiesList.add("Tây Ninh");
		citiesList.add("Thái Bình");
		citiesList.add("Thái Nguyên");
		citiesList.add("Thanh Hóa");
		citiesList.add("Thừa Thiên Huế");
		citiesList.add("Tiền Giang");
		citiesList.add("Trà Vinh");
		citiesList.add("Tuyên Quang");
		citiesList.add("Vĩnh Long");
		citiesList.add("Vĩnh Phúc");
		citiesList.add("Yên Bái");

		java.util.Map<String, String[]> districts = new java.util.HashMap<>();
		districts.put("Hà Nội", new String[] { 
			"-- Chọn quận/huyện --", "Ba Đình", "Hoàn Kiếm", "Đống Đa", "Cầu Giấy", "Thanh Xuân",
			"Hoàng Mai", "Hai Bà Trưng", "Long Biên", "Nam Từ Liêm", "Bắc Từ Liêm",
			"Tây Hồ", "Sơn Tây", "Ba Vì", "Chương Mỹ", "Đan Phượng"
		});
		districts.put("TP.HCM", new String[] { 
			"-- Chọn quận/huyện --", "Quận 1", "Quận 2", "Quận 3", "Quận 4", "Quận 5",
			"Quận 6", "Quận 7", "Quận 8", "Quận 9", "Quận 10",
			"Quận 11", "Quận 12", "Bình Thạnh", "Tân Bình", "Tân Phú",
			"Phú Nhuận", "Gò Vấp", "Bình Tân", "Củ Chi", "Hóc Môn"
		});
		districts.put("Đà Nẵng", new String[] { "-- Chọn quận/huyện --", "Hải Châu", "Thanh Khê", "Sơn Trà", "Ngũ Hành Sơn", "Liên Chiểu" });
		districts.put("Hải Phòng", new String[] { "-- Chọn quận/huyện --", "Hồng Bàng", "Ngô Quyền", "Lê Chân", "Hải An", "Kiến An" });
		districts.put("Cần Thơ", new String[] { "-- Chọn quận/huyện --", "Ninh Kiều", "Ô Môn", "Bình Thủy", "Cái Răng", "Thốt Nốt" });

		android.widget.ArrayAdapter<String> cityAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_item, citiesList);
		cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spCity.setAdapter(cityAdapter);
		spCity.setSelection(0);

		java.util.List<String> emptyDistrictList = new java.util.ArrayList<>();
		emptyDistrictList.add("-- Chọn quận/huyện --");
		android.widget.ArrayAdapter<String> emptyDistrictAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_item, emptyDistrictList);
		emptyDistrictAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spDistrict.setAdapter(emptyDistrictAdapter);
		spDistrict.setSelection(0);

		spCity.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
				if (position > 0) {
					String selectedCity = citiesList.get(position);
					String[] ds = districts.get(selectedCity);
					if (ds != null) {
						java.util.List<String> districtList = new java.util.ArrayList<>();
						for (String d : ds) districtList.add(d);
						android.widget.ArrayAdapter<String> dAdapter = new android.widget.ArrayAdapter<>(CheckoutActivity.this, android.R.layout.simple_spinner_item, districtList);
						dAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
						spDistrict.setAdapter(dAdapter);
						spDistrict.setSelection(0);
					} else {
						spDistrict.setAdapter(emptyDistrictAdapter);
						spDistrict.setSelection(0);
					}
				} else {
					spDistrict.setAdapter(emptyDistrictAdapter);
					spDistrict.setSelection(0);
				}
				estimateShippingIfReady();
			}

			@Override
			public void onNothingSelected(android.widget.AdapterView<?> parent) {}
		});

		spDistrict.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
				estimateShippingIfReady();
			}

			@Override
			public void onNothingSelected(android.widget.AdapterView<?> parent) {}
		});
	}

	private void confirmOrder() {
		// Validate inputs
		String receiverName = etReceiverName.getText().toString().trim();
		String receiverPhone = etReceiverPhone.getText().toString().trim();
		String shippingAddress = etShippingAddress.getText().toString().trim();
		int cityPos = spCity.getSelectedItemPosition();
		int districtPos = spDistrict.getSelectedItemPosition();

		if (receiverName.isEmpty()) {
			etReceiverName.setError("Vui lòng nhập tên người nhận");
			etReceiverName.requestFocus();
			return;
		}

		if (receiverPhone.isEmpty()) {
			etReceiverPhone.setError("Vui lòng nhập số điện thoại");
			etReceiverPhone.requestFocus();
			return;
		}

		if (receiverPhone.length() < 10) {
			etReceiverPhone.setError("Số điện thoại không hợp lệ");
			etReceiverPhone.requestFocus();
			return;
		}

		if (shippingAddress.isEmpty()) {
			etShippingAddress.setError("Vui lòng nhập địa chỉ giao hàng");
			etShippingAddress.requestFocus();
			return;
		}

		if (cityPos <= 0) {
			Toast.makeText(this, "Vui lòng chọn thành phố", Toast.LENGTH_SHORT).show();
			return;
		}

		String city = spCity.getSelectedItem().toString();
		String district = (districtPos > 0 ? spDistrict.getSelectedItem().toString() : "");
		String cityForApi = (district.isEmpty() ? "" : (district + ", ")) + city;

		// Get selected payment method
		int selectedPaymentId = rgPaymentMethod.getCheckedRadioButtonId();
		String paymentMethod = "cash"; // Default: COD
		
		if (selectedPaymentId == R.id.rbCOD) {
			paymentMethod = "cash";
		} else if (selectedPaymentId == R.id.rbBankTransfer) {
			paymentMethod = "bank_transfer";
		} else if (selectedPaymentId == R.id.rbPayOS) {
			paymentMethod = "payos";
		}

		// Show confirmation dialog
		showConfirmationDialog(receiverName, receiverPhone, shippingAddress + "\n" + cityForApi, paymentMethod);
	}

	private void showConfirmationDialog(String name, String phone, String address, String paymentMethod) {
		NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
		String formattedSubtotal = formatter.format(totalAmount) + " VNĐ";
		String formattedShipping = formatter.format(estimatedShippingFee) + " VNĐ";
		String formattedTotal = formatter.format(totalAmount + estimatedShippingFee) + " VNĐ";

		String message = "Người nhận: " + name + "\n" +
				"Số điện thoại: " + phone + "\n" +
				"Địa chỉ: " + address + "\n" +
				"Phương thức thanh toán: " + paymentMethod + "\n" +
				"Tạm tính: " + formattedSubtotal + "\n" +
				"Phí vận chuyển: " + formattedShipping + "\n" +
				"Tổng tiền: " + formattedTotal + "\n\n" +
				"Xác nhận đặt hàng?";

		new AlertDialog.Builder(this)
				.setTitle("Xác Nhận Đơn Hàng")
				.setMessage(message)
				.setPositiveButton("Xác nhận", (dialog, which) -> {
					// Process order
					processOrder(name, phone, address, paymentMethod);
				})
				.setNegativeButton("Hủy", null)
				.show();
	}

	private void processOrder(String name, String phone, String address, String paymentMethod) {
		// TODO: Save order to database
		// TODO: Clear cart
		// TODO: Send notification/email

		com.example.project.utils.AuthManager auth = com.example.project.utils.AuthManager.getInstance(this);
		com.example.project.network.ApiService api = com.example.project.network.RetrofitClient.getInstance().getApiService();
		com.example.project.models.User user = auth.getCurrentUser();
		if (user == null) {
			Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
			return;
		}

		java.util.Map<String, Object> body = new java.util.HashMap<>();
		body.put("userId", user.getId());
		java.util.Map<String, Object> shipping = new java.util.HashMap<>();
		shipping.put("fullName", name);
		shipping.put("phone", phone);
		shipping.put("address", address);
		int cityPos = spCity.getSelectedItemPosition();
		int districtPos = spDistrict.getSelectedItemPosition();
		String city = (cityPos > 0 ? spCity.getSelectedItem().toString() : "");
		String district = (districtPos > 0 ? spDistrict.getSelectedItem().toString() : "");
		String cityForApi = (district.isEmpty() ? "" : (district + ", ")) + city;
		shipping.put("city", cityForApi);
		body.put("shippingAddress", shipping);
		body.put("paymentMethod", paymentMethod);
		body.put("notes", "");

		api.createOrders(auth.getAuthHeader(), body).enqueue(new retrofit2.Callback<com.example.project.models.ApiResponse<Object>>() {
			@Override
			public void onResponse(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call, retrofit2.Response<com.example.project.models.ApiResponse<Object>> response) {
				if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
					// Nếu là PayOS, tạo payment link sau khi tạo order thành công
					if ("payos".equals(paymentMethod)) {
						handlePayOSPayment(response.body().getData());
						return;
					}
					
					// Các phương thức thanh toán khác
					String message = "Đơn hàng đã được tạo thành công.";
					String title = "Thành công";
					if ("cash".equals(paymentMethod)) {
						title = "Đặt hàng thành công";
						message = "Đơn hàng của bạn đã được tạo. Vui lòng chuẩn bị tiền mặt khi nhận hàng.";
					} else if ("bank_transfer".equals(paymentMethod)) {
						title = "Đặt hàng thành công";
						message = "Đơn hàng của bạn đã được tạo. Vui lòng chuyển khoản theo thông tin được gửi.";
					}
					
					try {
						Object data = response.body().getData();
						if (data instanceof java.util.Map) {
							java.util.Map d = (java.util.Map) data;
							Object summaryObj = d.get("summary");
							if (summaryObj instanceof java.util.Map) {
								java.util.Map summary = (java.util.Map) summaryObj;
								Object totalOrders = summary.get("totalOrders");
								Object totalAmount = summary.get("totalAmount");
								message += "\n\nTạo " + String.valueOf(totalOrders) + " đơn hàng. Tổng: " + formatCurrency(totalAmount) + " VNĐ";
							}
						}
					} catch (Exception ignored) {}

					new AlertDialog.Builder(CheckoutActivity.this)
						.setTitle(title)
						.setMessage(message)
						.setPositiveButton("OK", (dialog, which) -> finish())
						.setCancelable(false)
						.show();
				} else {
					String err = "Tạo đơn hàng thất bại. Vui lòng thử lại!";
					try {
						if (response.errorBody() != null) {
							String eb = response.errorBody().string();
							if (eb.contains("Thiếu tồn kho")) {
								err = "Cửa hàng không còn đủ xe cho một số sản phẩm.";
							}
						}
					} catch (Exception ignored) {}
					Toast.makeText(CheckoutActivity.this, err, Toast.LENGTH_LONG).show();
				}
			}

			@Override
			public void onFailure(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call, Throwable t) {
				Toast.makeText(CheckoutActivity.this, "Không thể tạo đơn hàng. Vui lòng thử lại!", Toast.LENGTH_LONG).show();
			}
		});
	}

	private String formatCurrency(Object v) {
		long val = 0;
		if (v instanceof Number) val = ((Number) v).longValue();
		NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
		return fmt.format(val);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == 1001) { // PayOS Payment Activity
			if (resultCode == RESULT_OK && data != null) {
				String paymentStatus = data.getStringExtra("paymentStatus");
				String orderId = data.getStringExtra("orderId");
				String code = data.getStringExtra("code");
				String orderCode = data.getStringExtra("orderCode");
				
				if ("success".equals(paymentStatus) && orderId != null) {
					// Thanh toán thành công - cập nhật Payment status
					confirmPaymentSuccess(orderId, code, orderCode);
				} else if ("cancelled".equals(paymentStatus)) {
					// Đã hủy thanh toán
					new AlertDialog.Builder(this)
						.setTitle("Đã hủy thanh toán")
						.setMessage("Bạn đã hủy thanh toán. Đơn hàng vẫn được tạo và đang chờ thanh toán.")
						.setPositiveButton("OK", (dialog, which) -> finish())
						.show();
				}
			} else if (resultCode == RESULT_CANCELED) {
				// User đã hủy
				Toast.makeText(this, "Đã hủy thanh toán", Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * Xác nhận thanh toán thành công và cập nhật Payment status
	 */
	private void confirmPaymentSuccess(String orderId, String code, String orderCode) {
		com.example.project.utils.AuthManager auth = com.example.project.utils.AuthManager.getInstance(this);
		com.example.project.network.ApiService api = com.example.project.network.RetrofitClient.getInstance().getApiService();
		
		java.util.Map<String, Object> body = new java.util.HashMap<>();
		body.put("code", code);
		body.put("orderCode", orderCode);
		body.put("status", "completed");
		
		api.confirmPayOSPayment(auth.getAuthHeader(), orderId, body).enqueue(new retrofit2.Callback<com.example.project.models.ApiResponse<Object>>() {
			@Override
			public void onResponse(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call, retrofit2.Response<com.example.project.models.ApiResponse<Object>> response) {
				if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
					// Thanh toán thành công - chuyển đến trang chi tiết đơn hàng
					new AlertDialog.Builder(CheckoutActivity.this)
						.setTitle("Thanh toán thành công")
						.setMessage("Đơn hàng của bạn đã được thanh toán thành công!")
						.setPositiveButton("OK", (dialog, which) -> {
							// Chuyển đến trang chi tiết đơn hàng để xem lại thông tin
							Intent intent = new Intent(CheckoutActivity.this, OrderDetailActivity.class);
							intent.putExtra("ORDER_ID", orderId);
							startActivity(intent);
							finish(); // Đóng CheckoutActivity sau khi chuyển
						})
						.setCancelable(false)
						.show();
				} else {
					// Lỗi khi cập nhật payment status
					android.util.Log.e("CheckoutActivity", "Error confirming payment: " + (response.errorBody() != null ? response.errorBody().toString() : "Unknown"));
					new AlertDialog.Builder(CheckoutActivity.this)
						.setTitle("Thanh toán thành công")
						.setMessage("Thanh toán đã được xử lý. Đơn hàng của bạn sẽ được cập nhật sớm.")
						.setPositiveButton("OK", (dialog, which) -> {
							// Vẫn chuyển đến trang chi tiết đơn hàng để xem lại thông tin
							Intent intent = new Intent(CheckoutActivity.this, OrderDetailActivity.class);
							intent.putExtra("ORDER_ID", orderId);
							startActivity(intent);
							finish(); // Đóng CheckoutActivity sau khi chuyển
						})
						.show();
				}
			}

			@Override
			public void onFailure(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call, Throwable t) {
				android.util.Log.e("CheckoutActivity", "Error confirming payment: " + t.getMessage(), t);
				// Vẫn hiển thị thành công vì PayOS đã xác nhận
				new AlertDialog.Builder(CheckoutActivity.this)
					.setTitle("Thanh toán thành công")
					.setMessage("Thanh toán đã được xử lý. Đơn hàng của bạn sẽ được cập nhật sớm.")
					.setPositiveButton("OK", (dialog, which) -> {
						// Vẫn chuyển đến trang chi tiết đơn hàng để xem lại thông tin
						Intent intent = new Intent(CheckoutActivity.this, OrderDetailActivity.class);
						intent.putExtra("ORDER_ID", orderId);
						startActivity(intent);
						finish(); // Đóng CheckoutActivity sau khi chuyển
					})
					.show();
			}
		});
	}

	/**
	 * Xử lý thanh toán PayOS sau khi tạo order thành công
	 */
	private void handlePayOSPayment(Object orderData) {
		try {
			if (!(orderData instanceof java.util.Map)) {
				Toast.makeText(this, "Lỗi dữ liệu đơn hàng", Toast.LENGTH_SHORT).show();
				return;
			}

			java.util.Map data = (java.util.Map) orderData;
			java.util.List orders = (java.util.List) data.get("orders");
			
			if (orders == null || orders.isEmpty()) {
				Toast.makeText(this, "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
				return;
			}

			// Lấy order đầu tiên để tạo payment link
			java.util.Map firstOrder = (java.util.Map) orders.get(0);
			java.util.Map order = (java.util.Map) firstOrder.get("order");
			
			if (order == null) {
				Toast.makeText(this, "Lỗi dữ liệu đơn hàng", Toast.LENGTH_SHORT).show();
				return;
			}

			String orderId = String.valueOf(order.get("_id"));
			
			// Tạo payment link
			com.example.project.utils.AuthManager auth = com.example.project.utils.AuthManager.getInstance(this);
			com.example.project.network.ApiService api = com.example.project.network.RetrofitClient.getInstance().getApiService();
			
			java.util.Map<String, Object> paymentBody = new java.util.HashMap<>();
			// Return URL và Cancel URL sẽ được backend tự động tạo từ FRONTEND_URL trong .env
			// Có thể để trống hoặc override nếu cần

			api.createPayOSPaymentLink(auth.getAuthHeader(), orderId, paymentBody).enqueue(new retrofit2.Callback<com.example.project.models.ApiResponse<Object>>() {
				@Override
				public void onResponse(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call, retrofit2.Response<com.example.project.models.ApiResponse<Object>> response) {
					if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
						try {
							Object data = response.body().getData();
							if (data instanceof java.util.Map) {
								java.util.Map d = (java.util.Map) data;
								java.util.Map paymentLink = (java.util.Map) d.get("paymentLink");
								
								if (paymentLink != null) {
									String checkoutUrl = String.valueOf(paymentLink.get("checkoutUrl"));
									
									// Mở WebView Activity để thanh toán PayOS trong app
									Intent paymentIntent = new Intent(CheckoutActivity.this, PayOSPaymentActivity.class);
									paymentIntent.putExtra("checkoutUrl", checkoutUrl);
									paymentIntent.putExtra("orderId", orderId);
									startActivityForResult(paymentIntent, 1001);
									return;
								}
							}
						} catch (Exception e) {
							android.util.Log.e("CheckoutActivity", "Error parsing PayOS response: " + e.getMessage(), e);
						}
					}
					
					// Nếu không tạo được payment link, vẫn hiển thị thông báo thành công
					Toast.makeText(CheckoutActivity.this, "Đơn hàng đã được tạo. Vui lòng thanh toán sau.", Toast.LENGTH_LONG).show();
					finish();
				}

				@Override
				public void onFailure(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call, Throwable t) {
					android.util.Log.e("CheckoutActivity", "Error creating PayOS payment link: " + t.getMessage(), t);
					Toast.makeText(CheckoutActivity.this, "Đơn hàng đã được tạo. Lỗi tạo link thanh toán: " + t.getMessage(), Toast.LENGTH_LONG).show();
					finish();
				}
			});
		} catch (Exception e) {
			android.util.Log.e("CheckoutActivity", "Error handling PayOS payment: " + e.getMessage(), e);
			Toast.makeText(this, "Lỗi xử lý thanh toán PayOS: " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
}

