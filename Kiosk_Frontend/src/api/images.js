import { apiClient } from "./client";

/**
 * 이미지 파일만 먼저 업로드하고 URL을 받아온다(주로 메뉴 "수정" 화면에서 이미지만 교체할 때 사용).
 * 메뉴 "등록" 시에는 createMenu/createSetMenu에 파일을 직접 첨부하는 편이 한 번의 요청으로 끝나 더 낫다.
 */
export const uploadMenuImage = (file) => {
  const formData = new FormData();
  formData.append("file", file);
  // FormData를 보낼 때는 Content-Type을 직접 건드리지 않아야 브라우저가 boundary를 포함해 자동으로 설정한다.
  return apiClient
    .post("/admin/images/menu", formData, { headers: { "Content-Type": undefined } })
    .then((r) => r.data);
};
