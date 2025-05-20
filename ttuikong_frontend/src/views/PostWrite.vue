<template>
  <div class="container">
    <!-- 헤더 -->
    <header class="header">
      <button class="back-btn" @click="confirmGoBack">
        <i class="icon-arrow-left"></i>
      </button>
      <h1 class="header-title">{{ isEditMode ? '게시글 수정' : '게시글 작성' }}</h1>
    </header>

    <!-- 로딩 상태 -->
    <div v-if="loading" class="loading-container">
      <div class="spinner"></div>
      <p>{{ isEditMode ? '게시글을 불러오는 중...' : '로딩 중...' }}</p>
    </div>

    <!-- 작성 폼 카드 -->
    <div v-else class="write-form">
      <!-- 제목 입력 -->
      <div class="form-group">
        <label for="title" class="form-label">제목</label>
        <div class="input-wrapper">
          <input 
            type="text" 
            id="title"
            v-model="title" 
            placeholder="제목을 입력하세요" 
            maxlength="100"
          >
        </div>
      </div>
      
      <!-- 내용 입력 -->
      <div class="form-group">
        <label for="content" class="form-label">내용</label>
        <div class="textarea-wrapper">
          <textarea 
            id="content"
            v-model="content" 
            placeholder="내용을 입력하세요..."
          ></textarea>
        </div>
        
        <!-- 이미지 업로드 -->
        <div class="image-upload">
          <div 
            v-show="!imagePreviewUrl" 
            class="image-placeholder" 
            @click="triggerImageUpload"
          >
            <i class="ri-camera-line"></i>
            <span>사진 추가</span>
          </div>
          <div 
            v-show="imagePreviewUrl" 
            class="image-preview"
          >
            <img :src="imagePreviewUrl" alt="미리보기">
            <button class="image-remove" @click="removeImage">
              <i class="ri-delete-bin-line"></i>
            </button>
          </div>
          <input 
            type="file" 
            ref="imageInput" 
            accept="image/*" 
            style="display: none;" 
            @change="handleImageUpload"
          >
        </div>
      </div>
      
      <!-- 카테고리 선택 -->
      <div class="form-group">
        <div class="form-label">카테고리</div>
        <div class="category-options">
          <div 
            v-for="category in categories" 
            :key="category.value"
            class="category-option" 
            :class="{ active: selectedCategory === category.value }"
            @click="selectCategory(category.value)"
          >
            {{ category.label }}
          </div>
        </div>
      </div>
      
      <!-- 위치 선택 -->
      <div class="form-group">
        <div class="form-label">위치</div>
        <div class="location-input" @click="toggleLocationSearch">
          <i class="ri-map-pin-line"></i>
          <span>{{ locationDisplay }}</span>
        </div>
        
        <div class="location-results" :class="{ show: showLocationSearch }">
          <div class="input-wrapper">
            <input 
              type="text" 
              v-model="locationKeyword" 
              placeholder="위치 검색..." 
              @input="searchLocation"
            >
          </div>
          <div 
            v-for="(location, index) in locationResults" 
            :key="index"
            class="location-item" 
            @click="selectLocation(location)"
          >
            {{ location }}
          </div>
        </div>
      </div>
      
      <!-- 태그 입력 -->
      <div class="form-group">
        <div class="form-label">태그</div>
        <div class="tag-input-container">
          <div class="input-wrapper tag-input-wrapper">
            <input 
              type="text" 
              v-model="tagInput" 
              class="tag-input" 
              placeholder="태그를 입력하세요 (최대 5개)"
              @keypress.enter.prevent="addTag"
            >
          </div>
          <button 
            class="btn btn-primary tag-add-btn" 
            :disabled="!canAddTag" 
            @click="addTag"
          >
            추가
          </button>
        </div>
        
        <div class="tags-list">
          <div 
            v-for="(tag, index) in tags" 
            :key="index" 
            class="tag"
          >
            {{ tag }}
            <span class="tag-remove" @click="removeTag(tag)">
              <i class="ri-close-line"></i>
            </span>
          </div>
        </div>
      </div>
      
      <!-- 완료 버튼 (하단에 위치) -->
      <div class="form-actions">
        <button 
          class="btn btn-primary submit-btn" 
          :disabled="!isFormValid || isSubmitting" 
          @click="submitPost"
        >
          {{ isSubmitting ? (isEditMode ? '수정 중...' : '등록 중...') : (isEditMode ? '게시글 수정' : '게시글 등록') }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import axios from 'axios';

// 라우터 설정
const router = useRouter();
const route = useRoute();

// API 기본 URL
const API_URL = 'http://localhost:8080/api';

// Refs
const imageInput = ref(null);
const title = ref('');
const content = ref('');
const imageFile = ref(null);
const imagePreviewUrl = ref('');
const selectedCategory = ref('자유');
const location = ref('');
const locationKeyword = ref('');
const showLocationSearch = ref(false);
const tagInput = ref('');
const tags = ref([]);
const isSubmitting = ref(false);
const loading = ref(false);

const token = localStorage.getItem('jwt');

// 편집 모드 관련
const postId = ref(null);
const originalImageUrl = ref(''); // 원본 이미지 URL 저장

const categories = [
  { value: '자유', label: '자유' },
  { value: '루틴공유', label: '루틴공유' },
  { value: '운동후기', label: '운동후기' },
  { value: '장소추천', label: '장소추천' },
  { value: '노래추천', label: '노래추천' },
  { value: '건강식품', label: '건강식품' }
];

// 위치 검색 결과 (실제 구현에서는 API 호출 결과로 대체)
const locationResults = ref([
  '한강공원 여의도',
  '강남구 역삼동',
  '서울 마포구 홍대입구',
  '서울특별시 성북구 안암동'
]);

// Computed 속성
const isEditMode = computed(() => {
  return !!route.params.id;
});

const isFormValid = computed(() => {
  return title.value.trim() && content.value.trim();
});

const canAddTag = computed(() => {
  return tagInput.value.trim() && tags.value.length < 5;
});

const locationDisplay = computed(() => {
  return location.value || '위치 추가';
});

const hasUnsavedChanges = computed(() => {
  return title.value.trim() || 
         content.value.trim() || 
         tags.value.length > 0 || 
         imageFile.value !== null ||
         location.value !== '';
});

// 편집 모드일 때 기존 게시글 데이터 로드
async function loadPostData() {
  if (!isEditMode.value) return;
  
  loading.value = true;
  
  try {
    const response = await axios.get(`${API_URL}/board/${route.params.id}`);
    const post = response.data;
    
    // 폼에 기존 데이터 채우기
    title.value = post.title || '';
    content.value = post.content || '';
    selectedCategory.value = post.category || '자유';
    location.value = post.location || '';
    
    // 기존 이미지가 있는 경우
    if (post.imageUrl || post.image_url) {
      originalImageUrl.value = post.imageUrl || post.image_url;
      imagePreviewUrl.value = originalImageUrl.value;
    }
    
    // 태그가 있는 경우 (문자열이면 배열로 변환, 배열이면 그대로 사용)
    if (post.tags) {
      if (typeof post.tags === 'string') {
        tags.value = post.tags.split(',').map(tag => tag.trim()).filter(tag => tag);
      } else if (Array.isArray(post.tags)) {
        tags.value = [...post.tags];
      }
    }
    
    postId.value = parseInt(route.params.id, 10);
    
    console.log('게시글 데이터 로드 완료:', post);
    
  } catch (error) {
    console.error('게시글 로드 중 오류:', error);
    alert('게시글을 불러오는데 실패했습니다.');
    router.push('/board');
  } finally {
    loading.value = false;
  }
}

function confirmGoBack() {
  if (hasUnsavedChanges.value) {
    if (confirm('작성 중인 내용이 있습니다. 정말 나가시겠습니까?')) {
      goBack();
    }
  } else {
    goBack();
  }
}

function goBack() {
  if (isEditMode.value) {
    router.push(`/board/${postId.value}`);
  } else {
    router.push('/board');
  }
}

function triggerImageUpload() {
  imageInput.value.click();
}

function handleImageUpload(event) {
  const file = event.target.files[0];
  if (!file) return;
  
  // 이미지 타입 확인
  if (!file.type.match('image.*')) {
    alert('이미지 파일만 업로드할 수 있습니다.');
    return;
  }
  
  // 이미지 크기 확인 (5MB 제한)
  if (file.size > 5 * 1024 * 1024) {
    alert('이미지 크기는 5MB 이하여야 합니다.');
    return;
  }
  
  imageFile.value = file;
  
  // 이미지 미리보기 생성
  const reader = new FileReader();
  reader.onload = (e) => {
    imagePreviewUrl.value = e.target.result;
  };
  reader.readAsDataURL(file);
}

function removeImage() {
  imageFile.value = null;
  imagePreviewUrl.value = '';
  originalImageUrl.value = '';
  imageInput.value.value = '';
}

function selectCategory(category) {
  selectedCategory.value = category;
}

function toggleLocationSearch() {
  showLocationSearch.value = !showLocationSearch.value;
  if (showLocationSearch.value) {
    nextTick(() => {
      const input = document.querySelector('.location-results input');
      if (input) input.focus();
    });
  }
}

function searchLocation() {
  // 실제 구현에서는 API를 통한 위치 검색 기능 구현
  // 현재는 예시 데이터로 필터링만 진행
  if (locationKeyword.value.trim() === '') {
    locationResults.value = [
      '한강공원 여의도',
      '강남구 역삼동',
      '서울 마포구 홍대입구',
      '서울특별시 성북구 안암동'
    ];
  } else {
    const keyword = locationKeyword.value.toLowerCase();
    locationResults.value = [
      '한강공원 여의도',
      '강남구 역삼동',
      '서울 마포구 홍대입구',
      '서울특별시 성북구 안암동'
    ].filter(loc => loc.toLowerCase().includes(keyword));
  }
}

function selectLocation(loc) {
  location.value = loc;
  showLocationSearch.value = false;
}

function addTag() {
  const tagText = tagInput.value.trim();
  
  if (!tagText) return;
  
  // 최대 5개 태그 제한
  if (tags.value.length >= 5) {
    alert('태그는 최대 5개까지만 추가할 수 있습니다.');
    return;
  }
  
  // 중복 태그 확인
  if (tags.value.includes(tagText)) {
    alert('이미 추가된 태그입니다.');
    return;
  }
  
  // 태그 추가
  tags.value.push(tagText);
  tagInput.value = '';
  
  // 태그 입력 필드에 포커스
  nextTick(() => {
    const input = document.querySelector('.tag-input');
    if (input) input.focus();
  });
}

function removeTag(tag) {
  tags.value = tags.value.filter(t => t !== tag);
}

async function submitPost() {
  // 입력 유효성 검사
  if (!title.value.trim()) {
    alert('제목을 입력해주세요.');
    return;
  }
  
  if (!content.value.trim()) {
    alert('내용을 입력해주세요.');
    return;
  }
  
  isSubmitting.value = true;
  
  try {
    // 게시글 데이터 객체 생성
    const boardData = {
      title: title.value.trim(),
      content: content.value.trim(),
      category: selectedCategory.value
    };
    
    // 선택적 필드들 추가
    if (location.value) {
      boardData.location = location.value;
    }
    
    if (tags.value.length > 0) {
      boardData.tags = tags.value;
    }
    
    // FormData 생성
    const formData = new FormData();
    
    const json = JSON.stringify(boardData);
    const blob = new Blob([json], {type:"application/json"});

    formData.append('board', blob);
    
    // 이미지 파일 추가 (있는 경우만)
    if (imageFile.value) {
      formData.append('image', imageFile.value);
    }
    
    console.log('전송할 데이터:');
    console.log('- boardData:', boardData);
    console.log('- image:', imageFile.value?.name);
    console.log('- isEditMode:', isEditMode.value);
    
    // API 호출 - 편집 모드에 따라 다른 엔드포인트 사용
    let response;
    if (isEditMode.value) {
      // 수정 API 호출
      response = await axios.put(`${API_URL}/board/${postId.value}`, formData, {
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'multipart/form-data'
        }
      });
      console.log('게시글 수정 성공:', response.data);
      alert('게시글이 수정되었습니다.');
      
      // 수정된 게시글 상세 페이지로 이동
      router.push(`/board/${postId.value}`);
    } else {
      // 작성 API 호출
      response = await axios.post(`${API_URL}/board/post`, formData, {
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'multipart/form-data'
        }
      });
      console.log('게시글 작성 성공:', response.data);
      alert('게시글이 작성되었습니다.');
      
      // 게시글 목록으로 이동
      router.push('/board');
    }
    
  } catch (error) {
    console.error('게시글 처리 중 오류가 발생했습니다:', error);
    
    // 상세 오류 정보 출력
    if (error.response) {
      console.log('오류 상태:', error.response.status);
      console.log('오류 데이터:', error.response.data);
      
      // 특정 오류에 대한 사용자 친화적 메시지
      if (error.response.status === 401) {
        alert('로그인이 만료되었습니다. 다시 로그인해 주세요.');
        localStorage.removeItem('userToken');
        router.push('/login');
      } else if (error.response.status === 400) {
        const errorMessage = error.response.data.message || '입력 데이터에 오류가 있습니다.';
        alert(`오류: ${errorMessage}`);
      } else if (error.response.status === 403) {
        alert('게시글을 수정할 권한이 없습니다.');
      } else if (error.response.status === 404) {
        alert('게시글을 찾을 수 없습니다.');
      } else {
        alert(`게시글 ${isEditMode.value ? '수정' : '작성'}에 실패했습니다. 다시 시도해 주세요.`);
      }
    } else {
      alert('네트워크 오류가 발생했습니다. 인터넷 연결을 확인해 주세요.');
    }
  } finally {
    isSubmitting.value = false;
  }
}

// 컴포넌트 마운트 시 편집 모드 확인 및 데이터 로드
onMounted(async () => {
  console.log('PostWrite 마운트, 편집 모드:', isEditMode.value);
  console.log('라우트 파라미터:', route.params.id);
  
  if (isEditMode.value) {
    await loadPostData();
  }
});
</script>

<style scoped>
/* 컨테이너 */
.container {
  max-width: 100%;
  margin: 0 auto;
  background-color: var(--background-color);
  min-height: 100vh;
  padding: 16px;
}

@media (min-width: 768px) {
  .container {
    max-width: 700px;
    margin: 0 auto;
  }
}

/* 로딩 컨테이너 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
}

.spinner {
  border: 4px solid rgba(0, 0, 0, 0.1);
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border-left-color: var(--primary-color);
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 헤더 스타일 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.back-btn {
  font-size: 24px;
  color: var(--primary-color);
  padding: 8px;
  background: none;
  border: none;
  cursor: pointer;
}

.header-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--dark-text);
  margin: 0 auto; /* 중앙 정렬 */
}

/* 작성 폼 카드 */
.write-form {
  background-color: var(--card-color);
  border-radius: var(--border-radius);
  overflow: hidden;
  box-shadow: var(--shadow-md);
  margin-bottom: 20px;
  padding: 16px;
  animation: fadeIn 0.4s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.form-group {
  margin-bottom: 24px;
}

.form-label {
  display: block;
  font-weight: 500;
  margin-bottom: 8px;
  color: var(--dark-text);
}

/* 폼 입력 필드 */
.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  border: 1px solid var(--border-color);
  border-radius: var(--input-radius);
  background-color: white;
  transition: var(--transition);
}

.input-wrapper:focus-within {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(255, 87, 34, 0.1);
}

.input-wrapper input {
  width: 100%;
  padding: 12px 16px;
  border: none;
  background: transparent;
  font-size: 1rem;
  color: var(--dark-text);
  font-family: inherit;
}

.input-wrapper input:focus {
  outline: none;
}

.textarea-wrapper {
  position: relative;
  border: 1px solid var(--border-color);
  border-radius: var(--input-radius);
  background-color: white;
  transition: var(--transition);
  margin-bottom: 16px;
}

.textarea-wrapper:focus-within {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(255, 87, 34, 0.1);
}

.textarea-wrapper textarea {
  width: 100%;
  padding: 12px 16px;
  border: none;
  font-size: 1rem;
  color: var(--dark-text);
  font-family: inherit;
  min-height: 150px;
  resize: vertical;
  background: transparent;
}

.textarea-wrapper textarea:focus {
  outline: none;
}

/* 이미지 업로드 영역 */
.image-upload {
  margin-top: 16px;
}

.image-placeholder {
  width: 100%;
  height: 120px;
  border: 2px dashed var(--primary-light);
  background-color: rgba(255, 87, 34, 0.05);
  border-radius: var(--border-radius);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: var(--primary-color);
  cursor: pointer;
  transition: var(--transition);
}

.image-placeholder:hover {
  background-color: rgba(255, 87, 34, 0.1);
  border-color: var(--primary-color);
}

.image-placeholder i {
  font-size: 28px;
  margin-bottom: 8px;
}

.image-preview {
  position: relative;
  margin-top: 16px;
}

.image-preview img {
  width: 100%;
  border-radius: var(--border-radius);
  display: block;
}

.image-remove {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: rgba(0, 0, 0, 0.6);
  color: white;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  border: none;
  cursor: pointer;
  transition: var(--transition);
}

.image-remove:hover {
  background-color: var(--danger-color);
  transform: scale(1.1);
}

/* 카테고리 선택 */
.category-options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 12px;
}

.category-option {
  padding: 8px 16px;
  background-color: #f5f5f5;
  border: 1px solid var(--border-color);
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: var(--transition);
}

.category-option:hover {
  background-color: rgba(255, 87, 34, 0.1);
  border-color: var(--primary-light);
}

.category-option.active {
  background-color: var(--primary-color);
  color: white;
  border-color: var(--primary-color);
  font-weight: 500;
}

/* 위치 선택 */
.location-input {
  display: flex;
  align-items: center;
  margin-top: 12px;
  padding: 10px 16px;
  background-color: #f5f5f5;
  border: 1px solid var(--border-color);
  border-radius: 20px;
  color: var(--medium-text);
  cursor: pointer;
  transition: var(--transition);
}

.location-input:hover {
  background-color: rgba(255, 87, 34, 0.05);
  border-color: var(--primary-light);
}

.location-input i {
  margin-right: 10px;
  color: var(--primary-color);
  font-size: 18px;
}

/* 위치 검색 결과 */
.location-results {
  margin-top: 12px;
  border-radius: var(--border-radius);
  overflow: hidden;
  box-shadow: var(--shadow-md);
  display: none;
  background-color: white;
}

.location-results.show {
  display: block;
}

.location-results .input-wrapper {
  border-radius: 0;
  border-left: none;
  border-right: none;
  border-top: none;
}

.location-item {
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-color);
  cursor: pointer;
  transition: var(--transition);
}

.location-item:last-child {
  border-bottom: none;
}

.location-item:hover {
  background-color: rgba(255, 87, 34, 0.05);
}

/* 태그 입력 */
.tag-input-container {
  display: flex;
  margin-top: 12px;
  gap: 10px;
}

.tag-input-wrapper {
  flex: 1;
  border-radius: var(--input-radius);
}

.tag-add-btn {
  padding: 8px 16px;
  border-radius: var(--input-radius);
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 16px;
}

.tag {
  background-color: rgba(255, 87, 34, 0.1);
  color: var(--primary-color);
  padding: 6px 28px 6px 12px;
  border-radius: 16px;
  font-size: 14px;
  position: relative;
}

.tag-remove {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  cursor: pointer;
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(255, 87, 34, 0.2);
  border-radius: 50%;
  transition: var(--transition);
}

.tag-remove:hover {
  background-color: var(--primary-color);
  color: white;
}

/* 완료 버튼 스타일 */
.form-actions {
  margin-top: 30px;
}

.submit-btn {
  width: 100%;
  padding: 14px;
  font-size: 16px;
  font-weight: 600;
  border-radius: var(--input-radius);
  background-color: var(--primary-color);
  color: white;
  border: none;
  cursor: pointer;
  transition: var(--transition);
  box-shadow: 0 4px 10px rgba(255, 87, 34, 0.2);
}

.submit-btn:hover:not(:disabled) {
  background-color: var(--primary-dark);
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(255, 87, 34, 0.3);
}

.submit-btn:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: 0 2px 5px rgba(255, 87, 34, 0.2);
}

.submit-btn:disabled {
  background-color: var(--light-text);
  cursor: not-allowed;
  box-shadow: none;
}
</style>