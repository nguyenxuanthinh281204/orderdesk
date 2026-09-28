# Strategy Pattern vs Conditional Logic Comparison

## 1. Comparing Designs

| Tiêu chí                | Thiết kế ban đầu (Step 1 - if/else)                                       | Thiết kế hoàn thiện (Strategy Pattern)                                   |
| :---------------------- | :------------------------------------------------------------------------ | :----------------------------------------------------------------------- |
| **Cơ chế phân loại**    | Chuỗi `if/else` duyệt qua chuỗi String (`tier`).                          | Đa hình (Polymorphism) qua interface `ShippingPolicy`.                   |
| **Khi thêm policy mới** | Phải sửa đổi trực tiếp vào thân hàm `shippingCost` trong `Checkout.java`. | Chỉ cần tạo một class mới implement `ShippingPolicy`, không sửa code cũ. |
| **Vi phạm SOLID**       | Vi phạm Open-Closed Principle (OCP) và Single Responsibility (SRP).       | Tuân thủ OCP (Open for extension, Closed for modification).              |
| **File bị tác động**    | `Checkout.java` (và các file test liên quan).                             | Chỉ file policy mới được thêm vào.                                       |

## 2. Git Diff Adding `OvernightShippingPolicy`

Diff khi thêm chính sách Overnight chứng minh `Checkout.java` hoàn toàn không bị chỉnh sửa:

```diff
commit feat: add OvernightShippingPolicy without modifying Checkout
Author: Developer ThinhNX9
Date:   2026-09-27

--- /dev/null
+++ b/orderdesk/src/main/java/com/fsa/orderdesk/checkout/OvernightShippingPolicy.java
@@ -0,0 +1,21 @@
+package com.fsa.orderdesk.checkout;
+
+import com.fsa.orderdesk.domain.Money;
+import com.fsa.orderdesk.domain.Order;
+import java.time.Instant;
+import java.time.temporal.ChronoUnit;
+
+public final class OvernightShippingPolicy implements ShippingPolicy {
+    public static final Money FEE = Money.of("150000", "VND");
+
+    @Override
+    public Money cost(Order order) {
+        return FEE;
+    }
+
+    @Override
+    public Instant promise(Instant placedAt) {
+        return placedAt.plus(0, ChronoUnit.DAYS);
+    }
+}
```
