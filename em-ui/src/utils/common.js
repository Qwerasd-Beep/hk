import { ElMessage,ElMessageBox } from "element-plus";
import "element-plus/es/components/message/style/index";
import "element-plus/es/components/message-box/style/index";
export const Const={
  LOCAL_TOKEN_NAME:'zhaoshang_token',
  HK_TOKEN:'hk_token'
}
export function msgBox(msg){
  ElMessage({
    showClose: true, //是否显示关闭按钮
    center: true, //文字是否居中
    duration: 3000, //显示时间，单位为毫秒
    message: msg, //提示的消息内容
    type: 'success', //消息类型：'success' | 'warning' | 'info' | 'error'
  })
}
//警告消息提示
			export function warnBox(msg) {
			    ElMessage({
			        showClose: true, 
			        center: true, 
			        message: msg, 
			        type: 'warning',
			    })
			}
			
			//普通消息提示
			export function infoBox(msg) {
			    ElMessage({
			        showClose: true, 
			        center: true, 
			        message: msg, 
			        type: 'info'
			    })
			}
			
			//错误消息提示
			export function errorBox(msg) {
			    ElMessage({
			        showClose: true, 
			        center: true,
			        message: msg, 
			        type: 'error', 
			        duration: 0 
			    })
			}
			
			//消息确认框
			export function confirmBox(msg) {
			    return ElMessageBox.confirm(
			        msg, //提示语
			        '系统提醒', //提示的标题
			        {
			            confirmButtonText: '确定',
			            cancelButtonText: '取消',
			            type: 'warning',
			        }
			    )
			}

export function clearToken(){
  window.sessionStorage.removeItem(Const.LOCAL_TOKEN_NAME)
  window.localStorage.removeItem(Const.LOCAL_TOKEN_NAME)
}
