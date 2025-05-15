// components/UserAvatar.vue
<template>
  <div class="user-avatar" :style="{ backgroundColor: bgColor }">
    <span v-if="!image">{{ initials }}</span>
    <img v-else :src="image" :alt="username" />
  </div>
</template>

<script>
export default {
  name: 'UserAvatar',
  props: {
    username: {
      type: String,
      required: true
    },
    image: {
      type: String,
      default: ''
    }
  },
  computed: {
    initials() {
      if (!this.username) return '';
      return this.username.split(' ')
        .map(name => name.charAt(0))
        .join('')
        .toUpperCase()
        .substring(0, 2);
    },
    bgColor() {
      // 사용자 이름에 기반한 일관된 컬러 생성
      const colors = [
        '#e91e63', '#9c27b0', '#673ab7', '#3f51b5', '#2196f3', 
        '#03a9f4', '#00bcd4', '#009688', '#4caf50', '#8bc34a',
        '#cddc39', '#ffc107', '#ff9800', '#ff5722'
      ];
      
      // 사용자 이름으로부터 간단한 해시 생성
      let hash = 0;
      for (let i = 0; i < this.username.length; i++) {
        hash = this.username.charCodeAt(i) + ((hash << 5) - hash);
      }
      
      // 컬러 배열에서 인덱스 선택
      const index = Math.abs(hash) % colors.length;
      return colors[index];
    }
  }
}
</script>

<style scoped>
.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  font-size: 16px;
  overflow: hidden;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>