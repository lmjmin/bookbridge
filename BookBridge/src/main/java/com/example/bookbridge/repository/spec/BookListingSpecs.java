package com.example.bookbridge.repository.spec;

import com.example.bookbridge.entity.BookListing;
import org.springframework.data.jpa.domain.Specification;

public class BookListingSpecs {

    public static Specification<BookListing> compose(
            String q, String department, Integer minPrice, Integer maxPrice,
            Boolean hasImage, String conditionText
    ) {
        Specification<BookListing> spec = Specification.where(null);

        if (q != null && !q.isBlank()) {
            String kw = "%" + q.trim().toLowerCase() + "%";
            spec = spec.and((root, cq, cb) -> cb.or(
                    cb.like(cb.lower(root.get("title")), kw),
                    cb.like(cb.lower(root.get("author")), kw),
                    cb.like(cb.lower(root.get("publisher")), kw)
            ));
        }

        // ✅ 학과 필터
        if (department != null && !department.isBlank()) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("department"), department));
        }

        if (minPrice != null) {
            spec = spec.and((root, cq, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice));
        }
        if (maxPrice != null) {
            spec = spec.and((root, cq, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice));
        }

        if (hasImage != null) {
            if (hasImage) {
                spec = spec.and((root, cq, cb) ->
                        cb.and(cb.isNotNull(root.get("imageUrl")),
                               cb.notEqual(cb.trim(root.get("imageUrl")), "")));
            } else {
                spec = spec.and((root, cq, cb) ->
                        cb.or(cb.isNull(root.get("imageUrl")),
                              cb.equal(cb.trim(root.get("imageUrl")), "")));
            }
        }

        if (conditionText != null && !conditionText.isBlank()) {
            String kw = "%" + conditionText.trim().toLowerCase() + "%";
            spec = spec.and((root, cq, cb) -> cb.like(cb.lower(root.get("conditionText")), kw));
        }
        return spec;
    }
}
