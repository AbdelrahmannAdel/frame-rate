import { useState } from "react";

// A 10-star rating widget.
// - Pass `value` + `onChange` to make it clickable (used for submitting a rating).
// - Pass only `value` (no onChange) to make it a read-only display
//   (used for showing someone else's rating).
function StarRating({ value, onChange, size = "normal" }) {
    const [hoverValue, setHoverValue] = useState(null);

    const isInteractive = typeof onChange === "function";
    const displayValue = hoverValue !== null ? hoverValue : value;

    const stars = [];
    for (let i = 1; i <= 10; i++) {
        stars.push(
            <span
                key={i}
                className={`star ${i <= displayValue ? "star-filled" : "star-empty"} ${size === "small" ? "star-small" : ""}`}
                onClick={isInteractive ? () => onChange(i) : undefined}
                onMouseEnter={isInteractive ? () => setHoverValue(i) : undefined}
                onMouseLeave={isInteractive ? () => setHoverValue(null) : undefined}
                style={isInteractive ? { cursor: "pointer" } : undefined}
            >
        ★
      </span>
        );
    }

    return (
        <span className="star-rating">
      {stars}
            {isInteractive && <span className="star-value">{displayValue}/10</span>}
    </span>
    );
}

export default StarRating;