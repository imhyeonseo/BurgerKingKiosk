// 장바구니 버튼의 목표 좌표(DOMRect) + "펄스 트리거" 이벤트를 위한 초경량 모듈 스코프 버스.
// 전역 상태(Zustand)에 일회성 UI 이벤트를 넣는 것은 과하므로 별도로 둔다(Frontend.md 8.4).

export const cartButtonRectRef = { current: null };

const listeners = new Set();

export function onCartPulse(handler) {
  listeners.add(handler);
  return () => listeners.delete(handler);
}

export function triggerCartButtonPulse() {
  listeners.forEach((handler) => handler());
}
