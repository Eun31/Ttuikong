<template>
  <div class="container">
    <!-- 헤더 -->
    <header class="header">
      <button class="back-btn" @click="confirmGoBack">
        <i class="icon-arrow-left"></i>
      </button>
      <h1 class="header-title">게시글 작성</h1>
    </header>

    <!-- 작성 폼 카드 -->
    <div class="write-form">
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
        <button class="btn btn-primary submit-btn" :disabled="!isFormValid" @click="submitPost">
          게시글 등록
        </button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PostWrite',
  data() {
    return {
      title: '',
      content: '',
      imageFile: null,
      imagePreviewUrl: '',
      selectedCategory: 'DAILY',
      categories: [
        { value: 'DAILY', label: '일상' },
        { value: 'TRAVEL', label: '여행' },
        { value: 'FOOD', label: '맛집' },
        { value: 'QUESTION', label: '질문' },
        { value: 'INFO', label: '정보' }
      ],
      location: '',
      locationKeyword: '',
      showLocationSearch: false,
      locationResults: [
        '한강공원 여의도',
        '강남구 역삼동',
        '서울 마포구 홍대입구',
        '서울특별시 성북구 안암동'
      ],
      tagInput: '',
      tags: []
    };
  },
  computed: {
    isFormValid() {
      return this.title.trim() && this.content.trim();
    },
    canAddTag() {
      return this.tagInput.trim() && this.tags.length < 5;
    },
    locationDisplay() {
      return this.location || '위치 추가';
    },
    hasUnsavedChanges() {
      return this.title.trim() || 
             this.content.trim() || 
             this.tags.length > 0 || 
             this.imageFile !== null ||
             this.location !== '';
    }
  },
  methods: {
    confirmGoBack() {
      if (this.hasUnsavedChanges) {
        if (confirm('작성 중인 내용이 있습니다. 정말 나가시겠습니까?')) {
          this.goBack();
        }
      } else {
        this.goBack();
      }
    },
    goBack() {
      this.$router.push('/board');
    },
    triggerImageUpload() {
      this.$refs.imageInput.click();
    },
    handleImageUpload(event) {
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
      
      this.imageFile = file;
      
      // 이미지 미리보기 생성
      const reader = new FileReader();
      reader.onload = (e) => {
        this.imagePreviewUrl = e.target.result;
      };
      reader.readAsDataURL(file);
    },
    removeImage() {
      this.imageFile = null;
      this.imagePreviewUrl = '';
      this.$refs.imageInput.value = '';
    },
    selectCategory(category) {
      this.selectedCategory = category;
    },
    toggleLocationSearch() {
      this.showLocationSearch = !this.showLocationSearch;
      if (this.showLocationSearch) {
        this.$nextTick(() => {
          this.$el.querySelector('.location-results input').focus();
        });
      }
    },
    searchLocation() {
      // 실제 구현에서는 API를 통한 위치 검색 기능 구현
      // 현재는 예시 데이터로 필터링만 진행
      if (this.locationKeyword.trim() === '') {
        this.locationResults = [
          '한강공원 여의도',
          '강남구 역삼동',
          '서울 마포구 홍대입구',
          '서울특별시 성북구 안암동'
        ];
      } else {
        const keyword = this.locationKeyword.toLowerCase();
        this.locationResults = [
          '한강공원 여의도',
          '강남구 역삼동',
          '서울 마포구 홍대입구',
          '서울특별시 성북구 안암동'
        ].filter(location => location.toLowerCase().includes(keyword));
      }
    },
    selectLocation(location) {
      this.location = location;
      this.showLocationSearch = false;
    },
    addTag() {
      const tagText = this.tagInput.trim();
      
      if (!tagText) return;
      
      // 최대 5개 태그 제한
      if (this.tags.length >= 5) {
        alert('태그는 최대 5개까지만 추가할 수 있습니다.');
        return;
      }
      
      // 중복 태그 확인
      if (this.tags.includes(tagText)) {
        alert('이미 추가된 태그입니다.');
        return;
      }
      
      // 태그 추가
      this.tags.push(tagText);
      this.tagInput = '';
      
      // 태그 입력 필드에 포커스
      this.$nextTick(() => {
        this.$el.querySelector('.tag-input').focus();
      });
    },
    removeTag(tag) {
      this.tags = this.tags.filter(t => t !== tag);
    },
    submitPost() {
      // 입력 유효성 검사
      if (!this.title.trim()) {
        alert('제목을 입력해주세요.');
        return;
      }
      
      if (!this.content.trim()) {
        alert('내용을 입력해주세요.');
        return;
      }
      
      // 게시글 데이터 수집
      const postData = {
        title: this.title.trim(),
        content: this.content.trim(),
        category: this.selectedCategory,
        location: this.location || null,
        tags: this.tags
      };
      
      // FormData 생성 (이미지 첨부를 위해)
      const formData = new FormData();
      
      // FormData에 데이터 추가
      Object.keys(postData).forEach(key => {
        if (key === 'tags') {
          // 배열은 JSON으로 변환하여 저장
          formData.append(key, JSON.stringify(postData[key]));
        } else {
          formData.append(key, postData[key]);
        }
      });
      
      // 이미지 파일 추가
      if (this.imageFile) {
        formData.append('image', this.imageFile);
      }
      
      // 개발 중이므로 콘솔에 출력하고 게시글 목록으로 이동
      console.log('전송할 데이터:', postData);
      alert('게시글이 작성되었습니다.');
      
      this.$router.push('/board');
    }
  }
};
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