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
const tagInput = ref('');
const tags = ref([]);
const isSubmitting = ref(false);
const loading = ref(false);
const imageRemoved = ref(false);

const token = localStorage.getItem('jwt');

const postId = ref(null);
const originalImageUrl = ref('');

const categories = [
  { value: '자유', label: '자유' },
  { value: '루틴공유', label: '루틴공유' },
  { value: '운동후기', label: '운동후기' },
  { value: '장소추천', label: '장소추천' },
  { value: '노래추천', label: '노래추천' },
  { value: '건강식품', label: '건강식품' }
];
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

const hasUnsavedChanges = computed(() => {
  return title.value.trim() || 
         content.value.trim() || 
         tags.value.length > 0 || 
         imageFile.value !== null
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
    
    // 이미지 처리
    if (post.imageUrl || post.image_url) {
      const imageUrl = post.imageUrl || post.image_url;
      originalImageUrl.value = imageUrl;
      
      if (imageUrl.startsWith('/uploads/')) {
        imagePreviewUrl.value = `${API_URL.replace('/api', '')}${imageUrl}`;
      } else {
        imagePreviewUrl.value = imageUrl;
      }
    }
    
    // 태그 처리
    if (post.tags) {
      if (typeof post.tags === 'string') {
        tags.value = post.tags.split(',').map(tag => tag.trim()).filter(tag => tag);
      } else if (Array.isArray(post.tags)) {
        tags.value = [...post.tags];
      }
    }
    
    postId.value = parseInt(route.params.id, 10);
    
    // 초기 상태 설정
    imageFile.value = null;
    imageRemoved.value = false;
    
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
  imageRemoved.value = true; // 명시적으로 삭제 의도 표시
  
  if (imageInput.value) {
    imageInput.value.value = '';
  }
}

function selectCategory(category) {
  selectedCategory.value = category;
}

function addTag() {
  const tagText = tagInput.value.trim();
  
  if (!tagText) return;
  
  if (tags.value.length >= 5) {
    alert('태그는 최대 5개까지만 추가할 수 있습니다.');
    return;
  }
  
  if (tags.value.includes(tagText)) {
    alert('이미 추가된 태그입니다.');
    return;
  }

  tags.value.push(tagText);
  tagInput.value = '';

  nextTick(() => {
    const input = document.querySelector('.tag-input');
    if (input) input.focus();
  });
}

function removeTag(tag) {
  tags.value = tags.value.filter(t => t !== tag);
}

async function submitPost() {
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
    const boardData = {
      title: title.value.trim(),
      content: content.value.trim(),
      category: selectedCategory.value
    };
    
    if (tags.value.length > 0) {
      boardData.tags = tags.value;
    }
    
    console.log('=== 제출 전 상태 확인 ===');
    console.log('imageFile:', imageFile.value?.name || 'null');
    console.log('imageRemoved:', imageRemoved.value);
    console.log('originalImageUrl:', originalImageUrl.value);
    
    let response;
    
    if (isEditMode.value) {
      const formData = new FormData();
      formData.append('board', new Blob([JSON.stringify(boardData)], {type: "application/json"}));
      
      // 새 이미지가 있는 경우 (삭제 후 새 이미지 포함)
      if (imageFile.value) {
        console.log('케이스: 새 이미지 업로드');
        formData.append('image', imageFile.value);
        // imageRemoved는 자동으로 false (기본값)
      } 
      // 이미지 삭제만 하는 경우 (새 이미지 없음)
      else if (imageRemoved.value && originalImageUrl.value) {
        console.log('케이스: 기존 이미지 삭제만');
        formData.append('imageRemoved', 'true');
      }
      // 그 외의 경우 (이미지 변경 없음)
      else {
        console.log('케이스: 이미지 변경 없음');
        // FormData에 아무것도 추가하지 않음 (기본값 사용)
      }
      
      // FormData 내용 확인
      console.log('=== FormData 내용 ===');
      for (let [key, value] of formData.entries()) {
        if (value instanceof File) {
          console.log(`${key}:`, value.name, `(${value.size} bytes)`);
        } else if (value instanceof Blob) {
          console.log(`${key}:`, 'Blob', `(${value.size} bytes)`);
        } else {
          console.log(`${key}:`, value);
        }
      }
      
      response = await axios.put(`${API_URL}/board/${postId.value}`, formData, {
        headers: {
          Authorization: `Bearer ${token}`
          // Content-Type은 브라우저가 자동 설정
        }
      });
      
      alert('게시글이 수정되었습니다.');
      router.push(`/board/${postId.value}`);
      
    } else {
      // 작성 모드
      console.log('케이스: 새 게시글 작성');
      const formData = new FormData();
      formData.append('board', new Blob([JSON.stringify(boardData)], {type: "application/json"}));
      
      if (imageFile.value) {
        formData.append('image', imageFile.value);
        console.log('새 게시글에 이미지 추가:', imageFile.value.name);
      }
      
      response = await axios.post(`${API_URL}/board/post`, formData, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      
      alert('게시글이 작성되었습니다.');
      router.push('/board');
    }
    
  } catch (error) {
    console.error('에러:', error);
    
    if (error.response) {
      const status = error.response.status;
      const message = error.response.data?.message || error.response.data;
      
      switch (status) {
        case 400:
          alert(`입력 오류: ${message}`);
          break;
        case 401:
          alert('로그인이 만료되었습니다.');
          localStorage.removeItem('jwt');
          router.push('/login');
          break;
        case 403:
          alert('권한이 없습니다.');
          break;
        case 404:
          alert('게시글을 찾을 수 없습니다.');
          break;
        case 415:
          console.error('415 에러 상세:', error.response);
          alert('요청 형식 오류입니다.');
          break;
        case 500:
          alert('서버 오류가 발생했습니다.');
          break;
        default:
          alert(`오류가 발생했습니다. (${status})`);
      }
    } else {
      alert('네트워크 오류가 발생했습니다.');
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