import { useEffect, useRef } from "react";
import { createPortal } from "react-dom";
import { IconUtensils } from "@/components/common/Icon";

/**
 * 장바구니 담기 성공 시 팝업 이미지가 하단 장바구니 버튼으로 날아가는 애니메이션.
 * Frontend.md 8장 참조 — 별도 애니메이션 라이브러리 없이 position:fixed + CSS transition으로 구현.
 * src가 null이면 기본 플레이스홀더(아이콘)를 렌더링해서 동일하게 날아간다.
 *
 * useState + rAF 패턴은 브라우저가 초기 opacity:1을 페인트하기 전에 상태가
 * 바뀌는 타이밍 이슈가 있다. 대신 ref로 DOM을 직접 잡고 getBoundingClientRect()로
 * reflow를 강제한 뒤 style을 변경해서 transition을 확실히 발동시킨다.
 */
export function FlyingImage({ src, startRect, endRect, onComplete }) {
  const elRef = useRef(null);

  useEffect(() => {
    const el = elRef.current;
    if (!el) return;

    // getBoundingClientRect() 호출이 reflow를 강제해서
    // 브라우저가 opacity:1 상태를 먼저 확정한다.
    el.getBoundingClientRect();

    const targetX = endRect.left + endRect.width / 2 - startRect.width / 2;
    const targetY = endRect.top + endRect.height / 2 - startRect.height / 2;

    el.style.transition =
      "transform 600ms cubic-bezier(0.25, 0.46, 0.45, 0.94), opacity 200ms ease 430ms";
    el.style.transform = `translate(${targetX - startRect.left}px, ${targetY - startRect.top}px) scale(0.15)`;
    el.style.opacity = "0";
  }, []);  // eslint-disable-line react-hooks/exhaustive-deps

  if (!startRect || !endRect) return null;

  const baseStyle = {
    left: startRect.left,
    top: startRect.top,
    width: startRect.width,
    height: startRect.height,
    opacity: 1,
    transform: "translate(0, 0) scale(1)",
    transition: "none",
  };

  const handleTransitionEnd = (e) => {
    if (e.propertyName === "opacity") onComplete();
  };

  return createPortal(
    src ? (
      <img
        ref={elRef}
        src={src}
        alt=""
        className="flying-image"
        style={baseStyle}
        onTransitionEnd={handleTransitionEnd}
      />
    ) : (
      <div
        ref={elRef}
        className="flying-image flying-image-placeholder"
        style={baseStyle}
        onTransitionEnd={handleTransitionEnd}
      >
        <IconUtensils size={Math.round(startRect.width * 0.4)} />
      </div>
    ),
    document.body
  );
}
