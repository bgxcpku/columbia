//
//  first_tryAppDelegate.h
//  first_try
//
//  Created by Frank Wood on 2/5/10.
//  Copyright Columbia University 2010. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface first_tryAppDelegate : NSObject <UIApplicationDelegate, UITabBarControllerDelegate> {
    UIWindow *window;
    UITabBarController *tabBarController;
}

@property (nonatomic, retain) IBOutlet UIWindow *window;
@property (nonatomic, retain) IBOutlet UITabBarController *tabBarController;

@end
