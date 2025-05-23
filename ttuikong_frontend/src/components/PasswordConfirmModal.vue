<!-- PasswordConfirmModal.vue -->
<template>
    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="modal-container" @click.stop>
        <div class="modal-header">
          <h3>비밀번호 확인</h3>
          <button class="close-btn" @click="closeModal">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
        
        <div class="modal-body">
          <p class="confirm-message">
            프로필 수정을 위해 현재 비밀번호를 입력해주세요.
          </p>
          
          <div class="input-group">
            <label for="current-password">현재 비밀번호</label>
            <div class="password-input-container">
              <input
                id="current-password"
                :type="showPassword ? 'text' : 'password'"
                v-model="password"
                placeholder="현재 비밀번호를 입력하세요"
                @keyup.enter="confirmPassword"
                :disabled="loading"
                class="password-input"
              />
              <button 
                type="button" 
                class="password-toggle"
                @click="togglePasswordVisibility"
                :disabled="loading"
              >
                <svg v-if="showPassword" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                  <line x1="1" y1="1" x2="23" y2="23"></line>
                </svg>
                <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                  <circle cx="12" cy="12" r="3"></circle>
                </svg>
              </button>
            </div>
          </div>
          
          <div v-if="error" class="error-message">
            {{ error }}
          </div>
        </div>
        
        <div class="modal-footer">
          <button 
            class="cancel-btn" 
            @click="closeModal"
            :disabled="loading"
          >
            취소
          </button>
          <button 
            class="confirm-btn" 
            @click="confirmPassword"
            :disabled="loading || !password.trim()"
          >
            <div v-if="loading" class="loading-spinner"></div>
            {{ loading ? '확인 중...' : '확인' }}
          </button>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, watch } from 'vue';
  
  const props = defineProps({
    showModal: {
      type: Boolean,
      default: false
    }
  });
  
  const emit = defineEmits(['close', 'confirm']);
  
  const password = ref('');
  const showPassword = ref(false);
  const loading = ref(false);
  const error = ref('');
  
  const togglePasswordVisibility = () => {
    showPassword.value = !showPassword.value;
  };
  
  const closeModal = () => {
    if (loading.value) return;
    
    password.value = '';
    showPassword.value = false;
    error.value = '';
    emit('close');
  };
  
  const confirmPassword = async () => {
    if (!password.value.trim()) {
      error.value = '비밀번호를 입력해주세요.';
      return;
    }
    
    if (password.value.length < 6) {
      error.value = '비밀번호는 최소 6자 이상이어야 합니다.';
      return;
    }
    
    loading.value = true;
    error.value = '';
    
    try {
      emit('confirm', password.value);
    } catch (err) {
      error.value = '비밀번호 확인 중 오류가 발생했습니다.';
      loading.value = false;
    }
  };

  watch(() => props.showModal, (newValue) => {
    if (newValue) {
      password.value = '';
      showPassword.value = false;
      error.value = '';
      loading.value = false;
    }
  });
  
  const setLoading = (isLoading) => {
    loading.value = isLoading;
  };
  
  const setError = (errorMessage) => {
    error.value = errorMessage;
    loading.value = false;
  };
  
  defineExpose({
    setLoading,
    setError
  });
  </script>
  
  <style scoped>
  .modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
    padding: 20px;
  }
  
  .modal-container {
    background-color: white;
    border-radius: 16px;
    box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
    width: 100%;
    max-width: 400px;
    max-height: 90vh;
    overflow-y: auto;
  }
  
  .modal-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 24px 24px 0 24px;
    margin-bottom: 16px;
  }
  
  .modal-header h3 {
    font-size: 18px;
    font-weight: 700;
    color: #333;
    margin: 0;
  }
  
  .close-btn {
    background: none;
    border: none;
    color: #666;
    cursor: pointer;
    padding: 4px;
    border-radius: 6px;
    transition: all 0.2s;
  }
  
  .close-btn:hover {
    background-color: #f1f1f1;
    color: #333;
  }
  
  .modal-body {
    padding: 0 24px 24px 24px;
  }
  
  .confirm-message {
    color: #666;
    font-size: 14px;
    line-height: 1.5;
    margin-bottom: 24px;
    text-align: center;
  }
  
  .input-group {
    margin-bottom: 16px;
  }
  
  .input-group label {
    display: block;
    font-size: 14px;
    font-weight: 600;
    color: #333;
    margin-bottom: 8px;
  }
  
  .password-input-container {
    position: relative;
  }
  
  .password-input {
    width: 100%;
    padding: 12px 48px 12px 16px;
    border: 2px solid #e1e5e9;
    border-radius: 12px;
    font-size: 14px;
    transition: border-color 0.2s;
    box-sizing: border-box;
  }
  
  .password-input:focus {
    outline: none;
    border-color: #FF7E36;
  }
  
  .password-input:disabled {
    background-color: #f8f9fa;
    color: #6c757d;
    cursor: not-allowed;
  }
  
  .password-toggle {
    position: absolute;
    right: 12px;
    top: 50%;
    transform: translateY(-50%);
    background: none;
    border: none;
    color: #666;
    cursor: pointer;
    padding: 4px;
    border-radius: 4px;
    transition: color 0.2s;
  }
  
  .password-toggle:hover:not(:disabled) {
    color: #FF7E36;
  }
  
  .password-toggle:disabled {
    cursor: not-allowed;
    opacity: 0.5;
  }
  
  .error-message {
    background-color: #fef2f2;
    border: 1px solid #fecaca;
    color: #dc2626;
    padding: 12px;
    border-radius: 8px;
    font-size: 14px;
    margin-top: 12px;
  }
  
  .modal-footer {
    display: flex;
    gap: 12px;
    padding: 0 24px 24px 24px;
  }
  
  .cancel-btn,
  .confirm-btn {
    flex: 1;
    padding: 12px 20px;
    border-radius: 12px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
    border: none;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
  }
  
  .cancel-btn {
    background-color: #f8f9fa;
    color: #6c757d;
  }
  
  .cancel-btn:hover:not(:disabled) {
    background-color: #e9ecef;
  }
  
  .cancel-btn:disabled {
    cursor: not-allowed;
    opacity: 0.6;
  }
  
  .confirm-btn {
    background-color: #FF7E36;
    color: white;
  }
  
  .confirm-btn:hover:not(:disabled) {
    background-color: #EF6926;
  }
  
  .confirm-btn:disabled {
    background-color: #cccccc;
    cursor: not-allowed;
  }
  
  .loading-spinner {
    width: 16px;
    height: 16px;
    border: 2px solid rgba(255, 255, 255, 0.3);
    border-top: 2px solid white;
    border-radius: 50%;
    animation: spin 1s linear infinite;
  }
  
  @keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
  }
  
  /* 반응형 */
  @media (max-width: 480px) {
    .modal-container {
      margin: 10px;
      max-width: none;
    }
    
    .modal-header {
      padding: 20px 20px 0 20px;
    }
    
    .modal-body {
      padding: 0 20px 20px 20px;
    }
    
    .modal-footer {
      padding: 0 20px 20px 20px;
      flex-direction: column;
    }
    
    .cancel-btn,
    .confirm-btn {
      width: 100%;
    }
  }
  </style>