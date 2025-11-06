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
		String paymentMethod = "vnpay"; // Chỉ dùng VNPay
        // Get selected payment method
        int selectedPaymentId = rgPaymentMethod.getCheckedRadioButtonId();
        String paymentMethod = "cash"; // Default
        
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
		body.put("paymentMethod", "vnpay");
		body.put("notes", "");
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("userId", user.getId());
        java.util.Map<String, Object> shipping = new java.util.HashMap<>();
        shipping.put("fullName", name);
        shipping.put("phone", phone);
        shipping.put("address", address);
        shipping.put("city", etCity.getText().toString().trim());
        body.put("shippingAddress", shipping);
        body.put("paymentMethod", paymentMethod);
        body.put("notes", "");

		api.createOrders(auth.getAuthHeader(), body).enqueue(new retrofit2.Callback<com.example.project.models.ApiResponse<Object>>() {
			@Override
			public void onResponse(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call, retrofit2.Response<com.example.project.models.ApiResponse<Object>> response) {
				if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
					String message = "Đơn hàng đã được tạo và đang chờ thanh toán VNPay.";
					try {
						Object data = response.body().getData();
						if (data instanceof java.util.Map) {
							java.util.Map d = (java.util.Map) data;
							Object summaryObj = d.get("summary");
							if (summaryObj instanceof java.util.Map) {
								java.util.Map summary = (java.util.Map) summaryObj;
								Object totalOrders = summary.get("totalOrders");
								Object totalAmount = summary.get("totalAmount");
								message = "Tạo " + String.valueOf(totalOrders) + " đơn hàng. Tổng: " + formatCurrency(totalAmount) + " VNĐ";
							}
						}
					} catch (Exception ignored) {}

					new AlertDialog.Builder(CheckoutActivity.this)
						.setTitle("Thanh toán VNPay")
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
        api.createOrders(auth.getAuthHeader(), body).enqueue(new retrofit2.Callback<com.example.project.models.ApiResponse<Object>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call, retrofit2.Response<com.example.project.models.ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    try {
                        Object data = response.body().getData();
                        if (data instanceof java.util.Map) {
                            java.util.Map d = (java.util.Map) data;
                            Object ordersObj = d.get("orders");
                            
                            if (ordersObj instanceof java.util.List) {
                                java.util.List orders = (java.util.List) ordersObj;
                                
                                // Nếu paymentMethod là payos, tạo payment link và redirect
                                if ("payos".equals(paymentMethod) && orders.size() > 0) {
                                    // Lấy order đầu tiên (hoặc có thể xử lý nhiều orders)
                                    Object firstOrder = orders.get(0);
                                    if (firstOrder instanceof java.util.Map) {
                                        java.util.Map orderMap = (java.util.Map) firstOrder;
                                        Object orderObj = orderMap.get("order");
                                        if (orderObj instanceof java.util.Map) {
                                            Object orderId = ((java.util.Map) orderObj).get("_id");
                                            if (orderId != null) {
                                                createPaymentLinkAndRedirect(orderId.toString(), name, phone, address);
                                                return;
                                            }
                                        }
                                    }
                                }
                                
                                // Nếu không phải payos hoặc lỗi, hiển thị thông báo thông thường
                                Object summaryObj = d.get("summary");
                                String message = "Đơn hàng đã được tạo thành công.";
                                if (summaryObj instanceof java.util.Map) {
                                    java.util.Map summary = (java.util.Map) summaryObj;
                                    Object totalOrders = summary.get("totalOrders");
                                    Object totalAmount = summary.get("totalAmount");
                                    message = "Tạo " + String.valueOf(totalOrders) + " đơn hàng. Tổng: " + formatCurrency(totalAmount) + " VNĐ";
                                }
                                
                                new AlertDialog.Builder(CheckoutActivity.this)
                                    .setTitle("Thành công")
                                    .setMessage(message)
                                    .setPositiveButton("OK", (dialog, which) -> finish())
                                    .setCancelable(false)
                                    .show();
                                return;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    
                    // Fallback message
                    new AlertDialog.Builder(CheckoutActivity.this)
                        .setTitle("Thành công")
                        .setMessage("Đơn hàng đã được tạo thành công.")
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
    
    /**
     * Tạo payment link và redirect user đến PayOS
     */
    private void createPaymentLinkAndRedirect(String orderId, String name, String phone, String address) {
        com.example.project.utils.AuthManager auth = com.example.project.utils.AuthManager.getInstance(this);
        com.example.project.network.ApiService api = com.example.project.network.RetrofitClient.getInstance().getApiService();
        
        // Tạo returnUrl và cancelUrl
        // Sử dụng BASE_URL từ backend (từ .env) 
        // Lưu ý: PayOS cần URL công khai (HTTP/HTTPS), không thể dùng localhost
        // Trong production, cần sử dụng URL thật từ backend (.env FRONTEND_URL hoặc BASE_URL)
        // PayOS sẽ redirect về returnUrl với query param orderCode
        String returnUrl = "https://folding-html-jeans-probability.trycloudflare.com/payment/success";
        String cancelUrl = "https://folding-html-jeans-probability.trycloudflare.com/payment/cancel";
        
        // Hoặc sử dụng deep link (cần cấu hình trong AndroidManifest.xml)
        // String packageName = getPackageName();
        // returnUrl = packageName + "://payment/success?orderCode={orderCode}";
        // cancelUrl = packageName + "://payment/cancel?orderCode={orderCode}";
        
        java.util.Map<String, Object> paymentBody = new java.util.HashMap<>();
        paymentBody.put("returnUrl", returnUrl);
        paymentBody.put("cancelUrl", cancelUrl);
        
        // Show loading
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setMessage("Đang tạo link thanh toán...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        
        api.createPaymentLink(auth.getAuthHeader(), orderId, paymentBody).enqueue(new retrofit2.Callback<com.example.project.models.ApiResponse<Object>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call, retrofit2.Response<com.example.project.models.ApiResponse<Object>> response) {
                progressDialog.dismiss();
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    try {
                        Object data = response.body().getData();
                        if (data instanceof java.util.Map) {
                            java.util.Map paymentData = (java.util.Map) data;
                            Object checkoutUrl = paymentData.get("checkoutUrl");
                            
                            if (checkoutUrl != null) {
                                // Mở WebView activity trong app để thanh toán PayOS
                                Intent paymentIntent = new Intent(CheckoutActivity.this, PayOSPaymentActivity.class);
                                paymentIntent.putExtra("checkoutUrl", checkoutUrl.toString());
                                paymentIntent.putExtra("orderId", orderId);
                                
                                // Start activity and wait for result
                                startActivityForResult(paymentIntent, 1001);
                                return;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    
                    Toast.makeText(CheckoutActivity.this, "Không thể tạo link thanh toán", Toast.LENGTH_SHORT).show();
                } else {
                    String err = "Không thể tạo link thanh toán. Vui lòng thử lại!";
                    try {
                        if (response.errorBody() != null) {
                            String eb = response.errorBody().string();
                            // Parse error message if needed
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(CheckoutActivity.this, err, Toast.LENGTH_LONG).show();
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(CheckoutActivity.this, "Lỗi kết nối. Vui lòng thử lại!", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 1001) {
            if (resultCode == RESULT_OK && data != null) {
                String paymentStatus = data.getStringExtra("paymentStatus");
                String orderId = data.getStringExtra("orderId");
                
                if ("success".equals(paymentStatus)) {
                    // Payment successful - PaymentResultActivity đã được mở từ PayOSPaymentActivity
                    // Có thể finish activity này hoặc show message
                    Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else if ("cancelled".equals(paymentStatus)) {
                    // Payment cancelled
                    Toast.makeText(this, "Thanh toán đã bị hủy", Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled payment
                Toast.makeText(this, "Thanh toán đã bị hủy", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

