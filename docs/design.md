# Domain Design Justification

| Concept         | Kind     | Justification                                                                                                                         |
| :-------------- | :------- | :------------------------------------------------------------------------------------------------------------------------------------ |
| **Sku**         | `record` | Là Value Object bất biến, định danh bằng chính giá trị text đại diện theo định dạng chuỗi quy chuẩn.                                  |
| **Money**       | `record` | Là Value Object bất biến đại diện cho tiền tệ (số tiền và đơn vị tiền), hai đối tượng cùng giá trị và đơn vị là như nhau.             |
| **OrderStatus** | `enum`   | Đại diện cho tập hợp hữu hạn cố định các trạng thái đơn hàng đã biết trước trong hệ thống.                                            |
| **OrderLine**   | `record` | Là Value Object bất biến đại diện cho một dòng chi tiết đơn hàng, chỉ mang dữ liệu tính toán và không có vòng đời độc lập.            |
| **Order**       | `class`  | Là một Entity (thực thể) có vòng đời biến thiên (chuyển trạng thái, thêm dòng), có định danh riêng (`id`) phân biệt thực thể độc lập. |

## Hash Code Contract Experiment

- **Hợp đồng Java**: Nếu hai đối tượng bằng nhau theo phương thức `equals()`, chúng **bắt buộc** phải có cùng giá trị `hashCode()`.
- **Hiện tượng khi xóa `hashCode()` trong `Order`**:
  Khi một đối tượng `Order(id=1)` được đưa vào `HashSet`, bộ băm tính toán vị trí bucket lưu trữ dựa trên giá trị `System.identityHashCode` (mặc định của `Object`). Khi dùng một đối tượng khác `Order(id=1)` để kiểm tra bằng `set.contains(...)`, dù `equals()` trả về `true`, `HashSet` lại tính ra mã băm khác và tìm kiếm ở một bucket rỗng khác, dẫn tới `set.contains(...)` trả về `false` sai thực tế.
