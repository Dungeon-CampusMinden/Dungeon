package graphic;

// @RunWith(PowerMockRunner.class)
// @PrepareForTest({DungeonCamera.class, OrthographicCamera.class})
public class DungeonCameraTest {
    /*
     DungeonCamera cam;
     Entity entity;

     PositionComponent pc;
     Vector3 pos;
     Frustum frustum;

     DungeonCamera cam_noFollow;
     Vector3 pos_noFollow;
     Frustum frustum_noFollow;

     @Before
     public void setUp() {
         entity = new Entity();
         pc = Mockito.mock(PositionComponent.class);
         pos = Mockito.mock(Vector3.class);
         frustum = Mockito.mock(Frustum.class);
         Mockito.when(pc.getPosition()).thenReturn(new Point(1, 1));
         PowerMockito.suppress(
                 MemberMatcher.constructor(OrthographicCamera.class, float.class, float.class));
         PowerMockito.suppress(MemberMatcher.method(OrthographicCamera.class, "update"));
         cam = PowerMockito.spy(new DungeonCamera(pc, 10, 10));
         Whitebox.setInternalState(cam, "position", pos);
         Whitebox.setInternalState(cam, "frustum", frustum);

         pos_noFollow = Mockito.mock(Vector3.class);
         frustum_noFollow = Mockito.mock(Frustum.class);
         cam_noFollow = PowerMockito.spy(new DungeonCamera(null, 10, 10));
         Whitebox.setInternalState(cam_noFollow, "position", pos_noFollow);
         Whitebox.setInternalState(cam_noFollow, "frustum", frustum_noFollow);
     }

     @Test
     public void test_update() {
         cam.update();
         Mockito.verify(cam).update();
         Mockito.verify(cam).getFollowedObject();
         Mockito.verify(pc).getPosition();
         Mockito.verify(cam.position).set(anyFloat(), anyFloat(), anyFloat());
         Mockito.verifyNoMoreInteractions(cam, pos, frustum, entity);
     }

     @Test
     public void test_update_noFollow() {
         cam_noFollow.update();
         Mockito.verify(cam_noFollow).update();
         Mockito.verify(cam_noFollow.position).set(anyFloat(), anyFloat(), anyFloat());
         Mockito.verifyNoMoreInteractions(cam_noFollow, pos_noFollow, frustum_noFollow);
     }

     @Test
     public void test_follow() {
         PositionComponent e2 = Mockito.mock(PositionComponent.class);

         cam.follow(e2);
         assertEquals(e2, cam.getFollowedObject());
         Mockito.verify(cam).follow(e2);
         Mockito.verify(cam).getFollowedObject();
         Mockito.verifyNoMoreInteractions(cam, pos, frustum, entity, e2);
     }

     @Test
     public void test_follow_noFollow() {
         cam_noFollow.follow(null);
         assertNull(cam_noFollow.getFollowedObject());
         Mockito.verify(cam_noFollow).follow(null);
         Mockito.verify(cam_noFollow).getFollowedObject();
         Mockito.verifyNoMoreInteractions(cam_noFollow, pos_noFollow, frustum_noFollow);
     }

     @Test
     public void test_getFollowedObject() {
         assertEquals(entity, cam.getFollowedObject());
         Mockito.verify(cam).getFollowedObject();
         Mockito.verifyNoMoreInteractions(cam, pos, frustum, entity);
     }

     @Test
     public void test_getFollowedObject_noFollow() {
         assertNull(cam_noFollow.getFollowedObject());
         Mockito.verify(cam_noFollow).getFollowedObject();
         Mockito.verifyNoMoreInteractions(cam_noFollow, pos_noFollow, frustum_noFollow);
     }

     @Test
     public void test_setFocusPoint() {
         cam.setFocusPoint(new Point(2, 2));
         assertNull(cam.getFollowedObject());
         Mockito.verify(cam).setFocusPoint(any());
         Mockito.verify(cam).getFollowedObject();
         Mockito.verifyNoMoreInteractions(cam, pos, frustum, entity);
     }

     @Test
     public void test_setFocusPoint_noFollow() {
         cam_noFollow.setFocusPoint(new Point(2, 2));
         assertNull(cam_noFollow.getFollowedObject());
         Mockito.verify(cam_noFollow).setFocusPoint(any());
         Mockito.verify(cam_noFollow).getFollowedObject();
         Mockito.verifyNoMoreInteractions(cam_noFollow, pos_noFollow, frustum_noFollow);
     }

     @Test
     public void test_isPointInFrustum() {
         cam.isPointInFrustum(2, 2);
         Mockito.verify(cam).isPointInFrustum(2, 2);
         Mockito.verify(frustum).boundsInFrustum(any());
         Mockito.verifyNoMoreInteractions(cam, pos, frustum, entity);
     }

     @Test
     public void test_isPointInFrustum_noFollow() {
         cam_noFollow.isPointInFrustum(2, 2);
         Mockito.verify(cam_noFollow).isPointInFrustum(2, 2);
         Mockito.verify(frustum_noFollow).boundsInFrustum(any());
         Mockito.verifyNoMoreInteractions(cam_noFollow, pos_noFollow, frustum_noFollow);
     }

     @Test
     public void test_getFrustum() {
         Mockito.verifyNoMoreInteractions(cam, pos, frustum, entity);
     }

     @Test
     public void test_getFrustum_noFollow() {
         Mockito.verifyNoMoreInteractions(cam_noFollow, pos_noFollow, frustum_noFollow);
     }
    */
}
