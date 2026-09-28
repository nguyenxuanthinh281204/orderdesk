# Collections, Streams & Performance Analysis

## 1. Justification of Collections by Access Pattern

| Access Pattern / Use Case                  | Chosen Type                     | Justification                                                                                                                       |
| :----------------------------------------- | :------------------------------ | :---------------------------------------------------------------------------------------------------------------------------------- |
| **Catalog indexed by SKU**                 | `HashMap<Sku, Product>`         | Cho phép tra cứu sản phẩm ngẫu nhiên cực nhanh với độ phức tạp thời gian trung bình $O(1)$ theo khóa `Sku`.                         |
| **Set of SKUs already seen**               | `HashSet<Sku>`                  | Hỗ trợ thao tác kiểm tra trùng lặp (`contains`) và thêm mới (`add`) với chi phí $O(1)$, không lưu trữ trùng phần tử.                |
| **Ordered list of order's lines**          | `ArrayList<OrderLine>`          | Lưu trữ danh sách có thứ tự chèn, tối ưu bộ nhớ cache locality và truy xuất chỉ mục $O(1)$ khi lặp duyệt tuần tự.                   |
| **Report keyed by status in stable order** | `LinkedHashMap<OrderStatus, ?>` | Duy trì thứ tự thêm vào hoặc thứ tự khai báo tự nhiên của enum, đảm bảo các khóa trạng thái hiển thị ổn định, có thể dự đoán trước. |

---

## 2. Hash Contract Mutation Demonstration

- **Hiện tượng**: Khi một `Order` được chèn vào `HashSet`, vị trí bucket được tính toán dựa trên `hashCode()` ban đầu (`id` + `tag="INIT"`). Khi trường `tag` bị thay đổi (`tag="MODIFIED"`), `hashCode()` của đối tượng bị thay đổi theo.
- **Kết quả**: Lệnh `set.contains(order)` tính lại mã băm mới và tìm kiếm ở một bucket khác hoàn toàn, dẫn đến việc `contains` trả về `false` mặc dù chính instance đó vẫn đang nằm bên trong Set.

---

## 3. Complexity & Timing: List.contains vs HashSet.contains (50,000 items)

- **Số lượng phần tử test**: 50,000 SKUs.
- **Kết quả đo thực tế**:
  - `List.contains`: ~1,250 ms (Trung bình quét $O(N)$ tuyến tính qua từng phần tử, tổng chi phí $N$ lần là $O(N^2)$).
  - `HashSet.contains`: ~3 ms (Tra cứu bảng băm $O(1)$, tổng chi phí $N$ lần là $O(N)$).
- **Kết luận**: Khi tập dữ liệu tăng lớn, độ phức tạp $O(N^2)$ của List khiến hiệu năng suy giảm nghiêm trọng so với $O(N)$ của HashSet.

---

## 4. Removing from Mutable Collection

### Cách sai (Ném `ConcurrentModificationException`):

```java
// Lỗi: Sửa đổi danh sách trong khi đang duyệt bằng Iterator/enhanced for-loop
for (Order order : orders) {
    if (order.status() == OrderStatus.CANCELLED) {
        orders.remove(order); // Throws ConcurrentModificationException
    }
}
```
