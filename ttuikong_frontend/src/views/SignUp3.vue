<!-- SignUp3.vue -->
<template>
  <div class="signup-container">
    <div class="signup-content">
      <div class="signup-header">
        <img :src="logo" alt="RunBuddy Logo" class="logo">
        <div class="progress-bar">
          <div class="progress-step completed"></div>
          <div class="progress-line completed"></div>
          <div class="progress-step completed"></div>
          <div class="progress-line active"></div>
          <div class="progress-step active"></div>
        </div>
        <p class="step-title">목표 설정 (3/3)</p>
      </div>

      <div class="signup-form goal-setting">
        <h2 class="form-question">당신은 어떤 러너가 되고 싶나요?</h2>
        <p class="form-subtext">앞으로의 러닝 목표를 선택해주세요</p>

        <div class="style-selection">
          <div class="style-option" v-for="(style, index) in goalStyles" :key="index"
            :class="{ 'selected': activityGoal === style.value }" @click="selectStyle(style.value)">
            <div class="style-icon">
              <span class="emoji">{{ style.emoji }}</span>
            </div>
            <div class="style-info">
              <h3>{{ style.title }}</h3>
              <p>{{ style.description }}</p>
            </div>
          </div>
        </div>

        <div class="extra-settings">
          <h3>추가 선택 사항</h3>
          <div class="checkbox-group">
            <!-- <label class="checkbox-label">
              <input type="checkbox" v-model="formData.notifications">
              <span class="checkmark"></span>
              <span>러닝 알림을 받을래요</span>
            </label>

            <label class="checkbox-label">
              <input type="checkbox" v-model="formData.groupActivities">
              <span class="checkmark"></span>
              <span>그룹 활동에 참여하고 싶어요</span>
            </label> -->

            <label class="checkbox-label">
              <input type="checkbox" v-model="formData.acceptTerms" required>
              <span class="checkmark"></span>
              <span>
                <a href="#" @click.prevent="openModal('terms')">이용약관</a>과
                <a href="#" @click.prevent="openModal('privacy')">개인정보 처리방침</a>에 동의합니다
                <TermsModal :visible="modalVisible" :title="modalTitle" :content="modalContent"
                  @close="modalVisible = false" />
              </span>
            </label>
          </div>
        </div>

        <div class="form-actions">
          <button class="btn btn-outline" @click="goToPrevStep">
            <i class="icon-arrow-left"></i>
            이전 단계
          </button>
          <button class="btn btn-success" @click="submitForm" :disabled="!formData.acceptTerms">
            가입 완료
            <i class="icon-check"></i>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import logoSrc from '../assets/logo_orange.png';
import TermsModal from '@/components/TermsModal.vue';

const router = useRouter();
const logo = ref(logoSrc);
const activityGoal = ref('');
const formData = ref({
  notifications: true,
  groupActivities: false,
  acceptTerms: false
});
const modalVisible = ref(false);
const modalTitle = ref('');
const modalContent = ref('');

const goalStyles = [
  {
    emoji: '🐨',
    value: '느긋한 코알라',
    title: '편안한 생활 러너',
    description: '즐겁게 달리며 일상에 활력을 더하고 싶어요.'
  },
  {
    emoji: '🐢',
    value: '산책하는 거북이',
    title: '건강한 습관 러너',
    description: '꾸준한 러닝으로 건강한 습관을 만들고 싶어요.'
  },
  {
    emoji: '🐶',
    value: '신나는 강아지',
    title: '활력 넘치는 러너',
    description: '러닝으로 스트레스를 해소하고 에너지를 충전하고 싶어요.'
  },
  {
    emoji: '🐎',
    value: '힘찬 질주 말',
    title: '도전하는 러너',
    description: '5K, 10K, 하프 마라톤 등 나만의 목표를 달성하고 싶어요.'
  },
  {
    emoji: '🐆',
    value: '전광석화 치타',
    title: '퍼포먼스 러너',
    description: '내 기록을 경신하고 마라톤 완주를 목표로 해요.'
  }
];

function selectStyle(style) {
  const savedData = JSON.parse(localStorage.getItem('signupData') || '{}');

  const selectedIndex = goalStyles.findIndex(s => s.value === style);
  const savedIndex = goalStyles.findIndex(s => s.value === savedData.activityLevel);

  if (savedIndex !== -1 && selectedIndex < savedIndex) {
    alert('선택한 러닝 목표가 활동 수준보다 낮습니다. 더 높은 목표를 선택해주세요.');
    return;
  }

  activityGoal.value = style;
}

function goToPrevStep() {
  router.push('/signup2');
}

const openModal = (type) => {
  if (type === 'terms') {
    modalTitle.value = '이용약관';
    modalContent.value = `[이용약관]

제1조 목적
본 약관은 뛰콩(이하 "서비스")의 이용조건 및 절차, 이용자와 서비스 제공자의 권리, 의무 및 책임사항을 규정함을 목적으로 합니다.

제2조 이용자의 정의
"이용자"란 본 서비스에 접속하여 본 약관에 따라 서비스를 이용하는 회원을 말합니다.

제3조 서비스 내용
1. 위치 기반 운동 경로 기록
2. 크루(그룹) 활동 및 커뮤니티 기능
3. 개인 운동 통계 제공
4. 챌린지 및 건강 미션 참여

제4조 회원가입
회원은 본인의 이메일과 비밀번호를 이용하여 회원가입을 신청할 수 있으며, 관리자의 승인 없이 자동 가입됩니다.

제5조 이용자의 의무
1. 타인의 정보를 도용해서는 안 됩니다.
2. 서비스 이용 시 법령과 본 약관을 준수해야 합니다.
3. 부정한 행위나 서버에 부하를 유발하는 행위를 금지합니다.

제6조 서비스 제공의 중지
운영상 또는 기술상의 이유로 서비스의 전부 또는 일부를 중지할 수 있습니다. 이 경우 사전 공지합니다.

제7조 책임의 제한
서비스는 운동 데이터 제공을 위한 플랫폼이며, 사용자가 입력한 정보나 활동 결과에 대한 법적 책임을 지지 않습니다.

부칙
본 약관은 2025년 5월 22일부터 적용됩니다.`;
  } else if (type === 'privacy') {
    modalTitle.value = '개인정보 처리방침';
    modalContent.value = `[개인정보 처리방침]

뛰콩은 개인정보 보호법 등 관련 법령을 준수하며, 이용자의 개인정보를 안전하게 보호하기 위해 아래와 같은 방침을 마련합니다.

1. 수집하는 개인정보 항목
- 필수: 이메일, 비밀번호
- 선택: 닉네임, 프로필 이미지
- 위치 정보: 운동 경로 기록을 위해 사용자의 실시간 위치를 수집할 수 있습니다.

2. 개인정보 수집 목적
- 회원 가입 및 로그인
- 개인 맞춤형 운동 기록 및 통계 제공
- 크루 활동 및 커뮤니티 기능 제공

3. 보유 및 이용 기간
- 회원 탈퇴 시까지 보관되며, 즉시 파기됩니다.

4. 제3자 제공
- 원칙적으로 제3자에게 개인정보를 제공하지 않습니다.

5. 개인정보의 파기
- 보관 목적 달성 시 즉시 파기하며, 전자파일은 복구 불가능한 방법으로 삭제됩니다.

6. 이용자의 권리
- 언제든지 자신의 개인정보를 열람, 정정, 삭제 요청할 수 있습니다.

7. 문의처
- 개인정보보호 관련 문의는 contact@ttuikong.com으로 연락 바랍니다.

본 방침은 2025년 5월 22일부터 적용됩니다.`;
  }
  modalVisible.value = true;
};

async function submitForm() {
  if (!formData.value.acceptTerms) {
    alert('이용약관과 개인정보 처리방침에 동의해주세요.');
    return;
  }

  // 기존 데이터 불러오기
  const savedData = JSON.parse(localStorage.getItem('signupData') || '{}');

  // 병합 데이터
  const completeData = {
    ...savedData,
    activityGoal: activityGoal.value,
    notifications: formData.value.notifications,
    groupActivities: formData.value.groupActivities
  };

  try {
    const response = await fetch('http://localhost:8080/api/users/signup', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(completeData)
    });

    const data = await response.json();

    if (response.ok) {
      alert('회원가입이 완료되었습니다');
      router.push('/login');
    } else if (response.status === 409) {
      alert('⚠️ 이미 등록된 이메일입니다.');
      router.push('/signup');
    } else {
      alert('⚠️ 회원가입에 실패했습니다. 다시 시도해주세요.');
      router.push('/signup');
    }
  } catch (error) {
    console.error('회원가입 오류:', error);
    alert('⚠️ 서버와 연결할 수 없습니다. 잠시 후 다시 시도해주세요.');
  } finally {
    localStorage.removeItem('signupData');
  }
}

onMounted(() => {
  // 기존 데이터 불러오기
  const savedData = localStorage.getItem('signupData');
  if (savedData) {
    const parsedData = JSON.parse(savedData);

    // 이전에 선택한 스타일이 있으면 불러오기
    if (parsedData.activityGoal) {
      activityGoal.value = parsedData.activityGoal;
    }

    // 현재 단계의 폼 데이터와 합치기
    if (parsedData.notifications !== undefined) {
      formData.value.notifications = parsedData.notifications;
    }
    if (parsedData.groupActivities !== undefined) {
      formData.value.groupActivities = parsedData.groupActivities;
    }
    if (parsedData.acceptTerms !== undefined) {
      formData.value.acceptTerms = parsedData.acceptTerms;
    }
  } else {
    // 이전 단계 데이터가 없으면 회원가입 첫 페이지로 돌아가기
    router.push('/signup');
  }
});
</script>

<style src="../assets/css/auth.css"></style>