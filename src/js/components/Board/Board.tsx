import { useSpring, animated } from '@react-spring/web'
import { useDrag } from '@use-gesture/react'

// export function Example() {
//   const [{ x, y }, api] = useSpring(() => ({ x: 0, y: 0 }))

//   // Set the drag hook and define component movement based on gesture data.
//   const bind = useDrag(({ down, movement: [mx, my] }) => {
//     api.start({ x: down ? mx : 0, y: down ? my : 0 })
//   })

//   // Bind it to a component.
//   return <animated.div {...bind()} style={{ x, y, touchAction: 'none' }} />
// }

export function Example({ children }) {
  const [{ x, y }, api] = useSpring(() => ({ x: 0, y: 0 }))
      console.log(children)

  // Set the drag hook and define component movement based on gesture data
  const bind = useDrag(({ down, movement: [mx, my] }) => {
    api.start({ x: down ? mx : 0, y: down ? my : 0, immediate: down })
  })

      // Bind it to a component
      return <animated.div {...bind()} style={{ x, y,  height: "30px", width: "40px"}} children={children}>
            </animated.div>
}
