// FRC2106 Junkyard Dogs - Continuity Base Code - www.team2106.org
// Modified by Greyson Weimer for FRC 2890 The Hawk Collection

package frc.robot.commands;

import frc.robot.subsystems.SwerveSubsystem;
import frc.robot.Constants.DriveConstants;
import frc.robot.Constants.IOConstants;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.filter.SlewRateLimiter;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;

public class SwerveJoystick extends Command
{
  // Create variables
  private final SwerveSubsystem swerveSubsystem;
  private final SlewRateLimiter xLimiter, yLimiter;
  private double initialHeading;
  private PIDController thetaController;
  
  // Command constructor
  public SwerveJoystick(SwerveSubsystem swerveSubsystem)
  {
    // Assign values passed from constructor
    this.swerveSubsystem = swerveSubsystem;
    
    // Slew rate limiter
    this.xLimiter = new SlewRateLimiter(DriveConstants.DRIVE_MAX_LINEAR_ACCELERATION);
    this.yLimiter = new SlewRateLimiter(DriveConstants.DRIVE_MAX_LINEAR_ACCELERATION);
    
    // Set default PID values for thetaPID
    thetaController = new PIDController(DriveConstants.PROPORTIOAL_THETA_CONTROLLER,
        DriveConstants.INTEGRAL_THETA_CONTROLLER,
        DriveConstants.DIFFERENTIAL_THETA_CONTROLLER);
    
    // Tell command that it needs swerveSubsystem
    addRequirements(swerveSubsystem);
  }
  
  @Override
  public void initialize()
  {
    initialHeading = swerveSubsystem.getHeading();
  }
  
  // Running loop of command
  @Override
  public void execute()
  {
    // Set speeds based on Joystick values
    double xSpeed = -IOConstants.DRIVER_JOYSTICK_COMMAND_JOYSTICK.getRawAxis(0);
    double ySpeed = IOConstants.DRIVER_JOYSTICK_COMMAND_JOYSTICK.getRawAxis(1);
    double turningSpeed = IOConstants.DRIVER_JOYSTICK_COMMAND_JOYSTICK.getRawAxis(3);
    
    // Check if joystick values are above deadzone
    if (Math.abs(xSpeed) > IOConstants.JOYSTICK_DEADZONE)
    {
      xSpeed = 0.0;
    }
    if (Math.abs(ySpeed) > IOConstants.JOYSTICK_DEADZONE)
    {
      ySpeed = 0.0;
    }
    if (Math.abs(turningSpeed) > IOConstants.JOYSTICK_DEADZONE)
    {
      turningSpeed = 0.0;
    }
    
    // Apply slew rate to joystick input to make robot input smoother and mulitply
    // by max speed
    xSpeed = xLimiter.calculate(xSpeed) * DriveConstants.DRIVE_MAX_LINEAR_SPEED;
    ySpeed = yLimiter.calculate(ySpeed) * DriveConstants.DRIVE_MAX_LINEAR_SPEED;
    
    // Set new heading
    initialHeading += turningSpeed;
    
    // Calculate turning speed to reach desired heading
    turningSpeed = thetaController.calculate(swerveSubsystem.getHeading(), initialHeading) * -100;
    
    // If we are not at the turning minimum, don't turn.
    if (turningSpeed < DriveConstants.TURNING_MINIMUM)
    {
      turningSpeed = 0.0;
    }
    
    // Limit turning speed
    if (turningSpeed > DriveConstants.DRIVE_MAX_ANGULAR_SPEED)
    {
      turningSpeed = DriveConstants.DRIVE_MAX_ANGULAR_SPEED;
    }
    else if (turningSpeed < -DriveConstants.DRIVE_MAX_ANGULAR_SPEED)
    {
      turningSpeed = -DriveConstants.DRIVE_MAX_ANGULAR_SPEED;
    }
    
    // Smartdashboard update
    SmartDashboard.putNumber("Turning Speed", turningSpeed);
    SmartDashboard.putNumber("Inital Heading", initialHeading);
    SmartDashboard.putNumber("NavX Heading", swerveSubsystem.getHeading());
    
    // Set the module state
    swerveSubsystem.setModuleStates(DriveConstants.kDriveKinematics
        .toSwerveModuleStates(ChassisSpeeds.fromFieldRelativeSpeeds(xSpeed, ySpeed, turningSpeed,
            Rotation2d.fromDegrees(swerveSubsystem.getRobotDegrees()))));
  }
  
  // Stop all module motor movement when command ends
  @Override
  public void end(boolean interrupted)
  {
    swerveSubsystem.stopModules();
  }
  
  @Override
  public boolean isFinished()
  {
    return false;
  }
  
}
