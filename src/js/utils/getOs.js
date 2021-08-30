export const getOs = (window) => {
  if (window.navigator.appVersion.includes('Windows')) {
    return 'windows'
  } else if (window.navigator.appVersion.includes('Mac')) {
    return 'mac'
  } else if (window.navigator.appVersion.includes('Linux')) {
    return 'linux'
  }
}