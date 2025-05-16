<template>
  <div class="container">
    <!-- 헤더 -->
    <header class="header">
      <button class="back-btn" @click="confirmGoBack">
        <i class="ri-arrow-left-line"></i>
      </button>
      <h1 class="header-title">게시글 작성</h1>
      <button class="submit-btn" :disabled="!isFormValid" @click="submitPost">완료</button>
    </header>

    <!-- 작성 폼 카드 -->
    <div class="write-card">
      <!-- 제목 입력 -->
      <div class="form-group">
        <input 
          type="text" 
          v-model="title" 
          class="form-input" 
          placeholder="제목을 입력하세요" 
          maxlength="100"
        >
      </div>
      
      <!-- 내용 입력 -->
      <div class="form-group">
        <textarea 
          v-model="content" 
          class="form-textarea" 
          placeholder="내용을 입력하세요..."
        ></textarea>
        
        <!-- 이미지 업로드 -->
        <div class="image-upload">
          <div 
            v-show="!imagePreviewUrl" 
            class="image-placeholder" 
            @click="triggerImageUpload"
          >
            <i class="ri-camera-line image-placeholder-icon"></i>
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
          <i class="ri-map-pin-line location-icon"></i>
          <span>{{ locationDisplay }}</span>
        </div>
        
        <div class="location-results" :class="{ show: showLocationSearch }">
          <input 
            type="text" 
            class="form-input" 
            v-model="locationKeyword" 
            placeholder="위치 검색..." 
            @input="searchLocation"
          >
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
          <input 
            type="text" 
            v-model="tagInput" 
            class="tag-input" 
            placeholder="태그를 입력하세요 (최대 5개)"
            @keypress.enter.prevent="addTag"
          >
          <button 
            class="tag-add-btn" 
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
      
      // 실제 서버 통신 코드 (주석 처리됨)
      /*
      axios.post('/api/posts', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      }).then(response => {
        // 성공 시 게시글 목록 페이지로 이동
        this.$router.push('/board');
      }).catch(error => {
        console.error('게시글 등록 오류:', error);
        alert('게시글 등록 중 오류가 발생했습니다.');
      });
      */
      
      // 개발 중이므로 콘솔에 출력하고 게시글 목록으로 이동
      console.log('전송할 데이터:', postData);
      alert('게시글이 작성되었습니다.');
      
      this.$router.push('/board');
    }
  },
  computed: {
    hasUnsavedChanges() {
      return this.title.trim() || 
             this.content.trim() || 
             this.tags.length > 0 || 
             this.imageFile !== null ||
             this.location !== '';
    }
  }
};
</script>

<style scoped>
/* 기본 리셋 및 공통 스타일은 App.vue에서 이미 적용되어 있으므로 여기서는 생략 */

/* 컨테이너 */
.container {
  max-width: 100%;
  margin: 0 auto;
  background-color: #f8f8f8;  /* 연한 회색 배경 */
  min-height: 100vh;
  padding: 12px;
}

@media (min-width: 600px) {
  .container {
    max-width: 600px;
    margin: 0 auto;
  }
}

/* 헤더 스타일 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.back-btn {
  font-size: 20px;
  color: #2e7d32;
  padding: 4px 8px;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #2e7d32;
}

.submit-btn {
  background-color: #4caf50;
  color: white;
  padding: 6px 12px;
  border-radius: 20px;
  font-weight: 600;
  font-size: 14px;
}

.submit-btn:disabled {
  background-color: #bdbdbd;
  cursor: not-allowed;
}

/* 작성 폼 카드 */
.write-card {
  background-color: white; /* 카드는 순수한 흰색으로 */
  border-radius: 20px;  /* 둥근 모서리 */
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1); /* 미세한 그림자 추가 */
  margin-bottom: 16px;
  border: 1px solid #e0e0e0; /* 연한 테두리 추가 */
}

.form-group {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.form-group:last-child {
  border-bottom: none;
}

.form-label {
  font-weight: 600;
  margin-bottom: 8px;
  color: #2e7d32;
}

/* 폼 입력 필드 */
.form-input {
  width: 100%;
  border: none;
  border-bottom: 1px solid #e0e0e0;
  padding: 8px 0;
  font-size: 16px;
  background-color: transparent;
  transition: border-color 0.3s;
}

.form-input:focus {
  outline: none;
  border-bottom-color: #4caf50;
}

.form-textarea {
  width: 100%;
  border: none;
  border-bottom: 1px solid #e0e0e0;
  padding: 8px 0;
  font-size: 15px;
  min-height: 150px;
  resize: none;
  background-color: transparent;
  transition: border-color 0.3s;
}

.form-textarea:focus {
  outline: none;
  border-bottom-color: #4caf50;
}

/* 이미지 업로드 영역 */
.image-upload {
  margin-top: 12px;
}

.image-placeholder {
  width: 100%;
  height: 120px;
  border: 2px dashed #c5e1c5;
  background-color: #f9fff9;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #4caf50;
  cursor: pointer;
}

.image-placeholder-icon {
  font-size: 24px;
  margin-bottom: 8px;
  color: #4caf50;
}

.image-preview {
  position: relative;
  margin-top: 12px;
}

.image-preview img {
  width: 100%;
  border-radius: 12px;
  display: block;
}

.image-remove {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: rgba(0, 0, 0, 0.5);
  color: white;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 카테고리 선택 */
.category-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.category-option {
  padding: 8px 12px;
  background-color: #f5f5f5;
  border: 1px solid #e0e0e0;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.category-option:hover {
  background-color: #e8f5e9;
  border-color: #c5e1c5;
}

.category-option.active {
  background-color: #4caf50;
  color: white;
  border-color: #4caf50;
  font-weight: 500;
}

/* 위치 선택 */
.location-input {
  display: flex;
  align-items: center;
  margin-top: 12px;
  padding: 8px 12px;
  background-color: #f5f5f5;
  border: 1px solid #e0e0e0;
  border-radius: 20px;
  color: #757575;
  cursor: pointer;
}

.location-icon {
  margin-right: 8px;
  color: #2e7d32;
}

/* 태그 입력 */
.tag-input-container {
  display: flex;
  margin-top: 12px;
}

.tag-input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #c5e1c5;
  border-radius: 20px 0 0 20px;
  font-size: 14px;
  background-color: #f9fff9;
}

.tag-input:focus {
  outline: none;
  border-color: #4caf50;
  background-color: white;
}

.tag-add-btn {
  background-color: #4caf50;
  color: white;
  padding: 8px 16px;
  border-radius: 0 20px 20px 0;
  font-size: 14px;
}

.tag-add-btn:disabled {
  background-color: #bdbdbd;
  cursor: not-allowed;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.tag {
  background-color: #e8f5e9;
  color: #2e7d32;
  padding: 4px 24px 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  position: relative;
}

.tag-remove {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 10px;
  cursor: pointer;
}

/* 위치 검색 결과 */
.location-results {
  margin-top: 8px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: none;
}

.location-results.show {
  display: block;
}

.location-item {
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
  background-color: white;
  cursor: pointer;
}

.location-item:last-child {
  border-bottom: none;
}

.location-item:hover {
  background-color: #f5f5f5;
}
</style>